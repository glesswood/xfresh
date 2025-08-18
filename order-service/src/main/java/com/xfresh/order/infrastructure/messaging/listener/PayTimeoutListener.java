/*
package com.xfresh.order.consumer;

import com.xfresh.dto.cmd.StockDeductCmd;
import com.xfresh.entity.Order;
import com.xfresh.entity.OrderItem;
//import com.xfresh.order.client.StockFeign;
import com.xfresh.order.repository.OrderItemRepository;
import com.xfresh.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.xfresh.order.config.RabbitPayTimeoutConfig.TIMEOUT_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayTimeoutListener {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    //private final StockFeign stockFeign;

    // 并发 1-2：建议在 yml 里配（下文有）
    @Transactional
    @RabbitListener(queues = TIMEOUT_QUEUE)
    public void onTimeout(Long orderId) {
        log.info("[订单超时检查] orderId={}", orderId);

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("订单不存在，直接ACK. id={}", orderId);
            return;
        }

        // 非待支付就不处理
        if (order.getStatus() != 1) {
            log.info("订单状态非待支付，跳过。id={}, status={}", orderId, order.getStatus());
            return;
        }

        // CAS：1 -> 0（取消）
        int changed = orderRepo.cancelIfPending(orderId);
        if (changed == 0) {
            log.info("CAS 未命中（可能并发已支付/已取消），跳过。id={}", orderId);
            return;
        }

        // 取消成功后，回滚库存
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        if (items.isEmpty()) {
            log.warn("订单明细为空，无法回滚库存。id={}", orderId);
            return;
        }

        List<StockDeductCmd.Item> revertItems = items.stream()
                .map(it -> new StockDeductCmd.Item(it.getProductId(), it.getQuantity()))
                .toList();

        //stockFeign.rollback(orderId, revertItems);
        log.info("订单超时已取消并回滚库存，id={}", orderId);
    }
}*//*

// order-service/src/main/java/com/xfresh/order/event/PayTimeoutListener.java
package com.xfresh.order.consumer;

import com.xfresh.dto.OrderDTO;
import com.xfresh.event.OrderEvent;
import com.xfresh.order.event.OrderEventBuilder;
import com.xfresh.entity.Order;
import com.xfresh.order.mapper.OrderMapper;
import com.xfresh.order.outbox.OutboxPublisher;
import com.xfresh.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayTimeoutListener {

    // 依赖：仅本地仓库 + Mapper + Outbox
    private final OrderRepository orderRepo;
    private final OrderMapper mapper;
    private final OutboxPublisher outboxPublisher;

    */
/**
     * 监听超时队列（延迟 5 分钟后投递的消息）
     * 消息体负载就是 orderId（字符串或数字都行，下面用字符串转 Long）。
     * 如果你有常量类，替换成常量即可；没有就直接写队列名字符串。
     *//*

    @RabbitListener(queues = "order.pay.timeout.q")
    @Transactional
    public void onTimeout(@Payload String orderIdStr) {
        Long orderId = null;
        try {
            orderId = Long.valueOf(orderIdStr);
        } catch (Exception e) {
            log.warn("[pay-timeout] 无法解析订单ID，payload={}", orderIdStr);
            return; // 丢弃这条异常消息
        }

        Order o = orderRepo.findById(orderId).orElse(null);
        if (o == null) {
            log.info("[pay-timeout] 订单不存在，orderId={}", orderId);
            return;
        }

        // 幂等：只有“待支付(1)”才允许自动取消
        if (o.getStatus() != 1) {
            log.info("[pay-timeout] 非待支付状态，跳过。orderId={}, status={}", orderId, o.getStatus());
            return;
        }

        // 条件更新：并发安全地把 1 -> 0
        int changed = orderRepo.updateStatusIfEquals(orderId, 1, 0);
        if (changed == 0) {
            log.info("[pay-timeout] 状态已被并发修改，跳过。orderId={}", orderId);
            return;
        }

        // 落一条取消事件到 Outbox（由 OutboxRelay 异步发 MQ）
        OrderDTO dto = mapper.toDto(o);
        List<OrderEvent.Item> eventItems = dto.getItems().stream()
                .map(i -> OrderEvent.Item.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build()
                )
                .toList();

        OrderEvent ev = OrderEventBuilder.cancelledFrom(o, eventItems);
        outboxPublisher.append(ev);

        log.info("[pay-timeout] 订单已超时取消并写出取消事件。orderId={}", orderId);
    }
}
*/

package com.xfresh.order.infrastructure.messaging.listener;

import com.xfresh.order.infrastructure.config.mq.RabbitPayTimeoutConfig;
import com.xfresh.order.infrastructure.outbox.OutboxPublisher;
import com.xfresh.order.domain.repository.OrderItemRepository;
import com.xfresh.order.domain.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;// PaymentTimeoutListener.java

import java.util.Map;

/// 注入订单明细仓库
@RequiredArgsConstructor
@Component
@Slf4j
public class PayTimeoutListener {
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;  // ← 新增
    private final OutboxPublisher outbox;

    @Transactional
    @RabbitListener(queues = RabbitPayTimeoutConfig.TIMEOUT_QUEUE)
    public void onTimeout(@Payload Map<String,Object> body) {
        Long orderId = ((Number) body.get("orderId")).longValue();

        var order = orderRepo.findById(orderId).orElse(null);
        if (order == null) { log.warn("[timeout] order not found {}", orderId); return; }
        if (order.getStatus() != 1) { // 1=待支付
            log.info("[timeout] already handled status={}, id={}", order.getStatus(), orderId);
            return;
        }

        // 1) 并发安全地取消（你已有的保存也行，最好用条件更新）
        order.setStatus(0); // 0=已取消
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderRepo.save(order);

        // 2) 查订单明细并映射为事件 items（非空）
        var items = orderItemRepo.findByOrderId(orderId);
        var eventItems = items.stream()
                .map(i -> com.xfresh.event.OrderEvent.Item.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .price(i.getPrice())
                        .build())
                .toList();

        // 3) 写取消事件到 outbox（带上 items，供 stock 回滚锁定）
        var ev = com.xfresh.event.OrderEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .type(com.xfresh.event.OrderEventType.ORDER_CANCELLED)
                .orderId(orderId)
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .occurredAt(java.time.LocalDateTime.now())
                .items(eventItems) // ★ 关键：不能为 null
                .build();
        outbox.append(ev);

        log.info("[timeout] cancelled & outbox appended, id={}, items={}", orderId, eventItems.size());
    }
}