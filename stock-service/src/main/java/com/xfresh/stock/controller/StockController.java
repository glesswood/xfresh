package com.xfresh.stock.controller;

import com.xfresh.common.ApiResponse;
import com.xfresh.stock.dto.StockDTO;
import com.xfresh.stock.dto.cmd.StockDeductCmd;
import com.xfresh.stock.entity.Stock;
import com.xfresh.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    /* 查询库存 */
    @GetMapping("/{pid}")
    public ApiResponse<StockDTO> get(@PathVariable Long pid) {
        return ApiResponse.ok(service.getByProductId(pid));
    }

   /* *//* 管理后台：直接修改库存（演示用） *//*
    @PostMapping("/{pid}/set")
    public ApiResponse<StockDTO> set(@PathVariable Long pid,
                                     @RequestParam Integer num) {
        Stock s = service.getByProductId(pid).toEntity(); // 自行写 mapper↔entity
        s.setTotalStock(num);
        return ApiResponse.ok(mapper.toDto(repo.save(s)));
    }*/
    /** ① Try：锁定库存 */
    @PostMapping("/lock")
    public ApiResponse<Void> lock(@RequestBody StockDeductCmd cmd){
        service.deductAndLock(cmd);
        return ApiResponse.ok();
    }

    /** ② Confirm：真正扣减 */
    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(@RequestParam Long orderId,
                                     @RequestBody List<StockDeductCmd.Item> items){
        service.confirm(orderId, items);
        return ApiResponse.ok();
    }

    /** ③ Cancel：回滚锁定 */
    @PostMapping("/rollback")
    public ApiResponse<Void> rollback(@RequestParam Long orderId,
                                      @RequestBody List<StockDeductCmd.Item> items){
        service.rollback(orderId, items);
        return ApiResponse.ok();
    }
}