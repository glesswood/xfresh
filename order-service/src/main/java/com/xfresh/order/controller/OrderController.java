package com.xfresh.order.controller;

import com.xfresh.order.dto.OrderDTO;
import com.xfresh.order.dto.cmd.OrderCreateCmd;
import com.xfresh.order.service.OrderService;
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
    public ApiResponse<OrderDTO> create(@RequestBody @Valid OrderCreateCmd cmd) {
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
}