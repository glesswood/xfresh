package com.xfresh.stock.service;

import com.xfresh.order.dto.StockDTO;
import com.xfresh.order.dto.cmd.StockDeductCmd;
import com.xfresh.order.dto.cmd.StockInitCmd;
import com.xfresh.stock.entity.Stock;

import java.util.List;

public interface StockService {

    /** 从 DB 扣减库存并锁定（下单时调用） */
    void lock(StockDeductCmd cmd);

    /** 支付成功 → 扣减 locked，减 total */
    void confirm(Long orderId, List<StockDeductCmd.Item> items);

    /** 支付失败/超时 → 回滚锁定 */
    void rollback(Long orderId, List<StockDeductCmd.Item> items);

    StockDTO getByProductId(Long productId);

    /**
     * 初始化（或重置）某个商品的可用库存
     */
    Stock init(StockInitCmd cmd);
}