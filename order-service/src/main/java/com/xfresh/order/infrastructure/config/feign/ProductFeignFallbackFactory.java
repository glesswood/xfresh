package com.xfresh.order.infrastructure.config.feign;

import com.xfresh.common.ApiResponse;
import com.xfresh.dto.ProductDTO;
import com.xfresh.order.infrastructure.client.ProductFeign;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

// com.xfresh.order.feign.fallback.ProductFeignFallbackFactory
@Component
public class ProductFeignFallbackFactory implements FallbackFactory<ProductFeign> {
    @Override
    public ProductFeign create(Throwable cause) {
        return new ProductFeign() {
            @Override
            public ApiResponse<ProductDTO> getById(Long id) {
                // 两种做法：A 直接返回失败响应；B 抛出业务异常交给全局异常处理
                // A：返回失败响应（前端拿到 code=1）
                return ApiResponse.of(1, "商品服务不可用，请稍后重试", null);

                // B：抛异常（如果你希望统一由 GlobalExceptionHandler 包装）
                // throw new BusinessException("商品服务不可用，请稍后重试");
            }
        };
    }
}