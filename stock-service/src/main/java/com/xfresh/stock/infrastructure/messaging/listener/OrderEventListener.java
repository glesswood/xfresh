package com.xfresh.stock.infrastructure.messaging.listener;

import com.xfresh.event.OrderEvent;
import com.xfresh.event.OrderEventType;
import com.xfresh.stock.infrastructure.config.mq.OrderEventAmqpConfig;
import com.xfresh.stock.infrastructure.idempotent.ProcessedEvent;
import com.xfresh.stock.infrastructure.idempotent.ProcessedEventRepository;
import com.xfresh.stock.application.service.OrderEventApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProcessedEventRepository peRepo;
    private final OrderEventApplyService applyService;

    @Transactional
    @RabbitListener(queues = OrderEventAmqpConfig.STOCK_QUEUE)
    public void onMessage(@Payload OrderEvent ev,
                          @Header(name = "amqp_receivedRoutingKey", required = false) String rk) {

        log.info("[order-event] rk={}, eventId={}, type={}, orderId={}",
                rk, ev.getEventId(), ev.getType(), ev.getOrderId());

        // 幂等：已处理直接返回
        if (peRepo.findByEventId(ev.getEventId()).isPresent()) {
            log.info("[order-event] duplicated, skip. eventId={}", ev.getEventId());
            return;
        }

        // 分发：基于枚举而不是 routing key 字符串
        OrderEventType type = ev.getType();
        switch (type) {
            case ORDER_CREATED -> applyService.onOrderCreated(ev);
            case ORDER_PAID    -> applyService.onOrderPaid(ev);
            case ORDER_CANCELLED -> applyService.onOrderCancelled(ev);
            default -> {
                log.warn("unknown event type: {}", type);
                return; // 不记录幂等，避免把未知事件吞掉
            }
        }

        // 记入幂等表
        peRepo.save(ProcessedEvent.builder()
                .eventId(ev.getEventId())
                .eventType(type.name())
                .aggregateId(ev.getOrderId())
                .build());
    }
}