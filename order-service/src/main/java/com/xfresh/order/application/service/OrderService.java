package com.xfresh.order.application.service;

import com.xfresh.dto.OrderDTO;
import com.xfresh.dto.cmd.OrderCreateCmd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /** 下单，返回完整订单 DTO（含明细） */
    OrderDTO create( OrderCreateCmd cmd);

    /** 根据主键查询 */
    OrderDTO findById(Long id);

    /** 用户订单分页 */
    Page<OrderDTO> pageByUser(Long userId, Pageable pageable);

    /** 取消订单（仅待支付状态可取消） */
    OrderDTO cancel(Long id);

    OrderDTO paySuccess(Long orderId);
}