package com.xfresh.order.client;

import com.xfresh.common.ApiResponse;
import com.xfresh.dto.ProductDTO;
import com.xfresh.order.config.feign.ProductFeignFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/api/products", fallbackFactory = ProductFeignFallbackFactory.class)
public interface ProductFeign {

    @GetMapping("/{id}")
    ApiResponse<ProductDTO> getById(@PathVariable("id") Long id);
}