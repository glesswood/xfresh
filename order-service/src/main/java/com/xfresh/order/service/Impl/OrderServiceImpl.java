package com.xfresh.order.service.Impl;   // impl 首字母小写习惯

import com.xfresh.common.ApiResponse;
import com.xfresh.exception.BusinessException;
import com.xfresh.exception.DuplicateRequestException;
import com.xfresh.order.client.ProductFeign;
import com.xfresh.order.client.StockFeign;
import com.xfresh.dto.OrderDTO;
import com.xfresh.dto.ProductDTO;
import com.xfresh.dto.cmd.OrderCreateCmd;
import com.xfresh.dto.cmd.StockDeductCmd;
import com.xfresh.order.entity.Order;
import com.xfresh.order.entity.OrderItem;
import com.xfresh.order.event.OrderEventPublisher;
import com.xfresh.order.event.PayTimeoutSender;
import com.xfresh.order.mapper.OrderMapper;
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

    private final StringRedisTemplate stringRedis;
    private final OrderRepository orderRepo;
    private final StockFeign stockFeign;
    private final OrderMapper mapper;
    private final OrderEventPublisher publisher;
    private final ProductFeign productFeign;
    private final PayTimeoutSender payTimeoutSender;

    /* ========== 创建订单 ========== *//*
    @Transactional
    @Override
    public OrderDTO create(OrderCreateCmd cmd) {

        *//* 0) 组装扣库存命令 *//*
        StockDeductCmd sdCmd = new StockDeductCmd(
                cmd.getItems().stream()
                        .map(i -> new StockDeductCmd.Item(i.getProductId(), i.getQuantity()))
                        .toList()
        );

        *//* 1) 锁库存 —— 失败抛异常，事务整体回滚 *//*
        stockFeign.lock(sdCmd);

        *//* 2) 保存订单 *//*
        Order entity = mapper.toEntity(cmd);
        entity.setOrderNo(genOrderNo());
        entity.setStatus(OrderStatus.PENDING);
        Order saved = orderRepo.save(entity);

        *//* 3) 返回 DTO *//*
        return mapper.toDto(saved);
    }
*/
    /** 下单（幂等：userId + requestId） */
    @Transactional
    @Override
    public OrderDTO create(OrderCreateCmd cmd) {

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
            // ② 预占库存（Lua 脚本在 stock-service）
            stockFeign.lock(toDeductCmd(cmd));

            // ③ 组装订单
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

            // ④ 落库（若唯一约束冲突，则兜底查询返回已有订单）
            Order saved;
            try {
                saved = orderRepo.save(order);
            } catch (DataIntegrityViolationException dup) {
                saved = orderRepo.findByUserIdAndRequestId(userId, reqId)
                        .orElseThrow(() -> dup);
            }

            // ⑤ 幂等键写回结果（可选优化，让重复请求直接命中并可返回 id）
            stringRedis.opsForValue().set(redisKey, "OK:" + saved.getId(), Duration.ofMinutes(30));

            // ⑥ 发布事件（MQ）
            publisher.created(mapper.toDto(saved));
            //7 发送延时消息
            payTimeoutSender.send(saved.getId());

            return mapper.toDto(saved);

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

    /* ========== 取消订单 ========== */
    @Override
    public OrderDTO cancel(Long id) {
        Order o = orderRepo.findById(id).orElseThrow();
        if (o.getStatus() != 1) throw new IllegalStateException("非待支付订单不能取消");
        o.setStatus(0);                                // 0=已取消
        orderRepo.save(o);

        stockFeign.rollback(id, o.itemsToStockCmd());  // 归还库存
        publisher.cancelled(mapper.toDto(o));
        return mapper.toDto(o);
    }

    @Override
    public OrderDTO paySuccess(Long orderId) {
        // 1) 查订单
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 2) 幂等/冲突判断
        if (order.getStatus() == 2) {
            // 已支付：幂等返回
            return mapper.toDto(order);
        }
        if (order.getStatus() == 0) {
            // 已取消：冲突
            throw new DuplicateRequestException("订单已取消，无法确认支付");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态非法，无法确认支付");
        }

        // 3) 取订单明细，构造确认库存的 items（不要信客户端）
        List<OrderItem> orderItems = order.getItems(); // 若没配置关联，改用 repo 查询
        // List<OrderItem> orderItems = orderItemRepo.findByOrderId(orderId);

        List<StockDeductCmd.Item> items = orderItems.stream()
                .map(oi -> new StockDeductCmd.Item(oi.getProductId(), oi.getQuantity()))
                .toList();

        // 4) 调用 stock-service 的 confirm (锁定 -> 真扣)
        ApiResponse<Void> resp = stockFeign.confirm(orderId, items);
        if (resp == null || resp.getCode() != 0) {
            // 失败直接抛，让事务回滚（不更改订单状态）
            throw new BusinessException("确认库存失败");
        }

        // 5) 条件更新：把状态从 1 -> 2，只允许成功一次（并发安全 & 幂等）
        int changed = orderRepo.updateStatusIfEquals(orderId, 1, 2);
        if (changed == 0) {
            // 并发下可能别的线程已更新，查最新返回
            Order fresh = orderRepo.findById(orderId)
                    .orElseThrow(() -> new BusinessException("订单不存在"));
            return mapper.toDto(fresh);
        }


        // 6) 返回
        Order updated = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        return mapper.toDto(updated);
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
        StockDeductCmd sdCmd = new StockDeductCmd(
                cmd.getItems().stream()
                        .map(i -> new StockDeductCmd.Item(i.getProductId(), i.getQuantity()))
                        .toList()
        );
        return sdCmd;
    }
}