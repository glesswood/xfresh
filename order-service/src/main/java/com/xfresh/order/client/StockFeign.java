package com.xfresh.order.client;

import com.xfresh.common.ApiResponse;
import com.xfresh.dto.cmd.StockDeductCmd;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// com.xfresh.order.remote.StockFeign.java
@FeignClient(name = "stock-service", path = "/api/stocks")
public interface StockFeign {

    @PostMapping("/lock")
    ApiResponse<Void> lock(@RequestBody StockDeductCmd cmd);

    @PostMapping("/confirm")
    ApiResponse<Void> confirm(@RequestParam("orderId") Long orderId,
                              @RequestBody List<StockDeductCmd.Item> items);

    @PostMapping("/rollback")
    ApiResponse<Void> rollback(@RequestParam("orderId") Long orderId,
                               @RequestBody List<StockDeductCmd.Item> items);
}