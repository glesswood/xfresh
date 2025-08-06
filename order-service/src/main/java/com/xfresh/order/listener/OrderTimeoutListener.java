package com.xfresh.order.listener;

import com.xfresh.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutListener {

    private final OrderService orderService;

    @RabbitListener(queues = "order.ttl.queue")  // 死信前原队列
    public void onTtl(Long orderId) {
        try {
            orderService.cancel(orderId);
            log.info("[订单超时已取消] {}", orderId);
        } catch (Exception e) {
            log.warn("取消超时订单失败 id={}", orderId, e);
        }
    }
}