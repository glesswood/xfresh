package com.xfresh.stock.service;

import com.xfresh.stock.dto.StockDTO;
import com.xfresh.stock.dto.cmd.StockDeductCmd;

import java.util.List;

public interface StockService {

    /** 从 DB 扣减库存并锁定（下单时调用） */
    void lock(StockDeductCmd cmd);

    /** 支付成功 → 扣减 locked，减 total */
    void confirm(Long orderId, List<StockDeductCmd.Item> items);

    /** 支付失败/超时 → 回滚锁定 */
    void rollback(Long orderId, List<StockDeductCmd.Item> items);

    StockDTO getByProductId(Long productId);
}