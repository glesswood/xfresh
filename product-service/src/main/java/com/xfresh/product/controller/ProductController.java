// product/controller/ProductController.java
package com.xfresh.product.controller;

import com.xfresh.common.ApiResponse;
import com.xfresh.product.dto.*;
import com.xfresh.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService service;

	@Operation(summary = "创建商品")
	@PostMapping
	public ApiResponse<ProductDTO> create(@Validated @RequestBody ProductCreateCmd cmd) {
		return ApiResponse.ok(service.create(cmd));
	}

	@Operation(summary = "更新商品")
	@PutMapping("/{id}")
	public ApiResponse<ProductDTO> update(@PathVariable Long id,
										  @Validated @RequestBody ProductUpdateCmd cmd) {
		cmd.setId(id);
		return ApiResponse.ok(service.update(cmd));
	}

	@Operation(summary = "商品详情")
	@GetMapping("/{id}")
	public ApiResponse<ProductDTO> get(@PathVariable Long id) {
		return ApiResponse.ok(service.get(id));
	}

	@Operation(summary = "商品列表")
	@GetMapping
	public ApiResponse<java.util.List<ProductDTO>> list() {
		return ApiResponse.ok(service.list());
	}

	@Operation(summary = "删除商品")
	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ApiResponse.ok();
	}
}