// com/xfresh/order/outbox/OutboxRelay.java
package com.xfresh.order.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xfresh.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {
    private final OutboxRepository repo;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;   // 直接注入

    @Scheduled(fixedDelay = 5000)
    public void relay() {
        var batch = repo.findTop200ByStatusOrderByIdAsc(0);
        if (batch.isEmpty()) return;

        for (OutboxEvent ev : batch) {
            try {
                // 1) 从 outbox 的 JSON 还原成对象
                OrderEvent eventObj = objectMapper.readValue(ev.getPayload(), OrderEvent.class);
                // 2) 发“对象”到交换机 + 路由键
                log.info("send -> exchange={}, rk={}, contentType=application/json, eventId={}",
                        ev.getExchangeName(), ev.getRoutingKey(), eventObj.getEventId());
                rabbitTemplate.convertAndSend(ev.getExchangeName(), ev.getRoutingKey(), eventObj);
                // 3) 标记已发送
                ev.setStatus(1);
                ev.setLastTriedAt(LocalDateTime.now());
                repo.save(ev);
            } catch (Exception e) {
                ev.setStatus(2);
                ev.setRetryCount((ev.getRetryCount()==null?0:ev.getRetryCount()) + 1);
                ev.setLastTriedAt(LocalDateTime.now());
                repo.save(ev);
            }
        }
    }
}