package com.xfresh.order.service.Impl;   // impl 首字母小写习惯

import com.xfresh.order.client.StockFeign;
import com.xfresh.order.constant.OrderStatus;
import com.xfresh.order.dto.OrderDTO;
import com.xfresh.order.dto.cmd.OrderCreateCmd;
import com.xfresh.order.dto.cmd.StockDeductCmd;
import com.xfresh.order.entity.Order;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /* ========== 创建订单 ========== */
    @Transactional
    @Override
    public OrderDTO create(OrderCreateCmd cmd) {

        /* 0) 组装扣库存命令 */
        StockDeductCmd sdCmd = new StockDeductCmd(
                cmd.getItems().stream()
                        .map(i -> new StockDeductCmd.Item(i.getProductId(), i.getQuantity()))
                        .toList()
        );

        /* 1) 锁库存 —— 失败抛异常，事务整体回滚 */
        stockFeign.lock(sdCmd);

        /* 2) 保存订单 */
        Order entity = mapper.toEntity(cmd);
        entity.setOrderNo(genOrderNo());
        entity.setStatus(OrderStatus.PENDING);
        Order saved = orderRepo.save(entity);

        /* 3) 返回 DTO */
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
    @Transactional
    @Override
    public OrderDTO cancel(Long id) {

        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("订单不存在"));

        // 只有待支付才能取消
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("仅待支付订单可取消");
        }

        // 回滚库存
        stockFeign.rollback(order.getId(), order.itemsToStockCmd());

        // 更新状态
        order.setStatus(OrderStatus.CANCELLED);
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
}