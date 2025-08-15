package com.xfresh.order.service.Impl;   // impl 首字母小写习惯

import com.xfresh.common.ApiResponse;
import com.xfresh.dto.OrderDTO;
import com.xfresh.dto.ProductDTO;
import com.xfresh.dto.cmd.OrderCreateCmd;
import com.xfresh.dto.cmd.StockDeductCmd;
import com.xfresh.event.OrderEvent;
import com.xfresh.order.event.OrderEventBuilder;
import com.xfresh.exception.BusinessException;
import com.xfresh.exception.DuplicateRequestException;
import com.xfresh.order.client.ProductFeign;
import com.xfresh.order.entity.Order;
import com.xfresh.order.entity.OrderItem;
import com.xfresh.order.event.PayTimeoutSender;
import com.xfresh.order.mapper.OrderMapper;
import com.xfresh.order.outbox.OutboxPublisher;
import com.xfresh.order.repository.OrderRepository;
import com.xfresh.order.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OutboxPublisher outboxPublisher;
    private final StringRedisTemplate stringRedis;
    private final OrderRepository orderRepo;
    private final OrderMapper mapper;
    private final ProductFeign productFeign;      // 只做“读”价格
    private final PayTimeoutSender payTimeoutSender;

    /** 下单（幂等：userId + requestId） */
    @Override
    public OrderDTO create(@Valid OrderCreateCmd cmd) {

        final Long userId  = cmd.getUserId();
        final String reqId = cmd.getRequestId(); // ★ 请求体里带的幂等键
        if (reqId == null || reqId.isBlank()) {
            throw new BusinessException("requestId 不能为空");
        }

        // 用 userId + requestId 作为幂等键（避免不同用户冲突）
        final String redisKey = "idem:order:create:" + userId + ":" + reqId;

        // ① 幂等：SETNX 占坑（5分钟按需）
        Boolean first = stringRedis.opsForValue().setIfAbsent(redisKey, "1", 5, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(first)) {
            throw new DuplicateRequestException("重复下单，请勿重复提交");
        }

        try {
            // （不再锁库）这里只做价格查询 + 计算金额 + 落订单

            // ② 组装订单
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(genOrderNo());
            order.setRequestId(reqId);                 // ★ 关键：落库，用于兜底查询
            order.setStatus(1);                        // 1=待支付
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> items = new ArrayList<>();

            for (OrderCreateCmd.ItemCmd itemCmd : cmd.getItems()) {
                // product-service 返回 ApiResponse<ProductDTO>
                ApiResponse<ProductDTO> resp = productFeign.getById(itemCmd.getProductId());
                ProductDTO product = (resp != null) ? resp.getData() : null;
                if (product == null || product.getPrice() == null) {
                    throw new BusinessException("获取商品价格失败，productId=" + itemCmd.getProductId());
                }

                BigDecimal sub = product.getPrice().multiply(BigDecimal.valueOf(itemCmd.getQuantity()));
                totalAmount = totalAmount.add(sub);

                OrderItem oi = new OrderItem();
                oi.setProductId(itemCmd.getProductId());
                oi.setQuantity(itemCmd.getQuantity());
                oi.setPrice(product.getPrice());
                oi.setCreateTime(LocalDateTime.now());
                oi.setUpdateTime(LocalDateTime.now());
                oi.setOrder(order); // 维护双向关系（多方持有一方）
                items.add(oi);
            }

            order.setTotalAmount(totalAmount);
            order.setItems(items);

            // ③ 落库（若唯一约束冲突，则兜底查询返回已有订单）
            Order saved;
            try {
                saved = orderRepo.save(order);
            } catch (DataIntegrityViolationException dup) {
                saved = orderRepo.findByUserIdAndRequestId(userId, reqId)
                        .orElseThrow(() -> dup);
            }

            // ④ 幂等键写回结果（可选优化，让重复请求直接命中并可返回 id）
            stringRedis.opsForValue().set(redisKey, "OK:" + saved.getId(), Duration.ofMinutes(30));

            // ⑤ 发送“支付超时”延迟消息（你已有的逻辑）
            payTimeoutSender.send(saved.getId());

            // ⑥ Outbox 记录 ORDER_CREATED（可选，如果你需要创建事件）
            OrderDTO dto = mapper.toDto(saved);
            // 生成事件明细（从实体映射，避免 DTO 命名差异）
            List<OrderEvent.Item> evItems = saved.getItems().stream()
                    .map(oi -> OrderEvent.Item.builder()
                            .productId(oi.getProductId())
                            .quantity(oi.getQuantity())
                            .price(oi.getPrice())
                            .build())
                    .toList();

// 构建并写入 outbox
            OrderEvent createdEv = OrderEventBuilder.createdFrom(saved, evItems);
            outboxPublisher.append(createdEv);

            return dto;

        } catch (Exception e) {
            // 失败允许重试：删同一个 redisKey
            stringRedis.delete(redisKey);
            throw e;
        }
    }

    /* ========== 查询 ========== */
    @Override
    public OrderDTO findById(Long id) {
        return orderRepo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在"));
    }

    @Override
    public Page<OrderDTO> pageByUser(Long userId, Pageable pageable) {
        return orderRepo.findByUserId(userId, pageable).map(mapper::toDto);
    }

    /* ========== 取消订单（发取消事件，不再同步回滚库存） ========== */
    @Override
    public OrderDTO cancel(Long id) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new BusinessException("订单不存在"));
        if (o.getStatus() != 1) {
            throw new BusinessException("非待支付订单不能取消");
        }

        // 先改状态为取消
        o.setStatus(0);
        orderRepo.save(o);

        // 构造事件明细
        List<OrderEvent.Item> evItems = o.getItems().stream()
                .map(oi -> OrderEvent.Item.builder()
                        .productId(oi.getProductId())
                        .quantity(oi.getQuantity())
                        .price(oi.getPrice())
                        .build())
                .toList();

        // 发 ORDER_CANCELLED 事件
        OrderEvent cancelledEv = OrderEventBuilder.cancelledFrom(o, evItems);
        outboxPublisher.append(cancelledEv);

        return mapper.toDto(o);
    }

    /* ========== 支付成功（发支付事件，不再同步确认库存） ========== */
    @Override
    @Transactional
    public OrderDTO paySuccess(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 幂等/冲突判断
        if (order.getStatus() == 2) return mapper.toDto(order);
        if (order.getStatus() == 0) throw new DuplicateRequestException("订单已取消，无法确认支付");
        if (order.getStatus() != 1) throw new BusinessException("订单状态非法，无法确认支付");

        // 1) 原子把 1 -> 2（已支付）
        int changed = orderRepo.updateStatusIfEquals(orderId, 1, 2, LocalDateTime.now());
        if (changed == 0) {
            // 并发下可能被超时/其它请求修改了，刷新判断一次
            order = orderRepo.findById(orderId).orElseThrow();
            if (order.getStatus() == 2) return mapper.toDto(order);
            if (order.getStatus() == 0) throw new DuplicateRequestException("订单已取消，无法确认支付");
            throw new BusinessException("并发冲突，请重试");
        }

        // 2) 构造事件明细（基于当前订单项）
        List<OrderEvent.Item> evItems = order.getItems().stream()
                .map(oi -> OrderEvent.Item.builder()
                        .productId(oi.getProductId())
                        .quantity(oi.getQuantity())
                        .price(oi.getPrice())
                        .build())
                .toList();

        // 3) 在同一事务里写出 ORDER_PAID 到 outbox
        OrderEvent paidEv = OrderEventBuilder.paidFrom(order, evItems);
        outboxPublisher.append(paidEv);

        return mapper.toDto(order);
    }

    /* ========== 工具方法 ========== */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    /** 生成 20 位左右订单号：时间戳 + 6 位随机数 */
    private String genOrderNo() {
        return FMT.format(LocalDateTime.now())
                + String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private StockDeductCmd toDeductCmd(OrderCreateCmd cmd) {
        return new StockDeductCmd(
                cmd.getItems().stream()
                        .map(i -> new StockDeductCmd.Item(i.getProductId(), i.getQuantity()))
                        .toList()
        );
    }
}