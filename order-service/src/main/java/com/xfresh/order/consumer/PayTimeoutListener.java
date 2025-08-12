package com.xfresh.order.consumer;

import com.xfresh.exception.BusinessException;
import com.xfresh.dto.cmd.StockDeductCmd;
import com.xfresh.order.entity.Order;
import com.xfresh.order.entity.OrderItem;
import com.xfresh.order.client.StockFeign;
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
    private final StockFeign stockFeign;

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

        stockFeign.rollback(orderId, revertItems);
        log.info("订单超时已取消并回滚库存，id={}", orderId);
    }
}