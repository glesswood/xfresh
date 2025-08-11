package com.xfresh.order.service.Impl;   // impl 首字母小写习惯

import com.xfresh.common.ApiResponse;
import com.xfresh.order.client.ProductFeign;
import com.xfresh.order.client.StockFeign;
import com.xfresh.order.dto.OrderDTO;
import com.xfresh.order.dto.ProductDTO;
import com.xfresh.order.dto.cmd.OrderCreateCmd;
import com.xfresh.order.dto.cmd.StockDeductCmd;
import com.xfresh.order.entity.Order;
import com.xfresh.order.entity.OrderItem;
import com.xfresh.order.event.OrderEventPublisher;
import com.xfresh.order.mapper.OrderMapper;
import com.xfresh.order.repository.OrderRepository;
import com.xfresh.order.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final StockFeign stockFeign;
    private final OrderMapper mapper;
    private final OrderEventPublisher publisher;
    private final ProductFeign productFeign;

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
    @Transactional
    @Override
    public OrderDTO create(OrderCreateCmd cmd) {

        /* ---------- 1. 预占库存 ---------- */
        stockFeign.lock(toDeductCmd(cmd));

        /* ---------- 2. 组装订单实体 ---------- */
        Order order = new Order();
        order.setUserId(cmd.getUserId());
        order.setOrderNo(genOrderNo());
        order.setStatus(1);                                  // 1 = 待支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderCreateCmd.ItemCmd itemCmd : cmd.getItems()) {

            // 2.1 远程查商品价格（包装体 → getData()）
            ApiResponse<ProductDTO> resp = productFeign.getById(itemCmd.getProductId());
            ProductDTO product = resp.getData();
            if (product == null || product.getPrice() == null) {
                throw new IllegalStateException("获取商品价格失败，productId=" + itemCmd.getProductId());
            }

            // 2.2 计算金额 & 组装明细
            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(itemCmd.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(itemCmd.getProductId());
            oi.setQuantity(itemCmd.getQuantity());
            oi.setPrice(product.getPrice());
            oi.setCreateTime(LocalDateTime.now());
            oi.setUpdateTime(LocalDateTime.now());
            oi.setOrder(order);          // 关键：维护双向关系

            items.add(oi);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(items);           // 关键：让 JPA 级联保存明细

        /* ---------- 3. 落库 ---------- */
        Order saved = orderRepo.save(order);

        /* ---------- 4. MQ 事件 ---------- */
        publisher.created(mapper.toDto(saved));

        /* ---------- 5. 返回 DTO ---------- */
        return mapper.toDto(saved);
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