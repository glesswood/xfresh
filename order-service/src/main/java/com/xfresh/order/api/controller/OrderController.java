package com.xfresh.order.api.controller;

import com.xfresh.dto.OrderDTO;
import com.xfresh.dto.cmd.OrderCreateCmd;
import com.xfresh.exception.DuplicateRequestException;
import com.xfresh.order.application.service.OrderService;
import com.xfresh.common.ApiResponse;          // 统一返回包装
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;


    @PostMapping
    public ApiResponse<OrderDTO> create(
            @Valid @RequestBody OrderCreateCmd cmd) {
        return ApiResponse.ok(service.create(cmd));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderDTO> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> get(@PathVariable Long id) {
        return ApiResponse.ok(service.findById(id));
    }

    @GetMapping("/page")
    public ApiResponse<Page<OrderDTO>> page(
            @RequestParam Long userId,
            Pageable pageable) {
        return ApiResponse.ok(service.pageByUser(userId, pageable));
    }

    /** 支付成功回调：根据订单ID确认支付并扣减锁定库存 */
    @PostMapping("/{id}/pay-success")
    public ApiResponse<OrderDTO> paySuccess(@PathVariable Long id) {
        return ApiResponse.ok(service.paySuccess(id));
    }

    @GetMapping("/_test/dup")
    public void testDup() {
        throw new DuplicateRequestException("测试重复");
    }
}