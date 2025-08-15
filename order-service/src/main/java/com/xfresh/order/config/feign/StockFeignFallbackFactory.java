/*
package com.xfresh.order.config.feign;


import com.xfresh.common.ApiResponse;
import com.xfresh.exception.BusinessException;
import com.xfresh.dto.cmd.StockDeductCmd;
import com.xfresh.order.client.StockFeign;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory; // 如果这里报错，换成：import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FallbackFactory; // ↑ 用这个
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockFeignFallbackFactory implements FallbackFactory<StockFeign> {
    private static final Logger log = (Logger) LoggerFactory.getLogger(StockFeignFallbackFactory.class);
    @Override
    public StockFeign create(Throwable cause) {

        return new StockFeign() {
            @Override
            public ApiResponse<Void> lock(StockDeductCmd cmd) {
                // 你也可以记录 cause 到日志里
                log.error("[降级] stockFeign.lock 失败: {}", cause.toString(), cause);
                throw new BusinessException("库存服务不可用，请稍后重试");
            }
            @Override
            public ApiResponse<Void> confirm(Long orderId, List<StockDeductCmd.Item> items) {
                log.error("[降级] stockFeign.confirm 失败: {}", cause.toString(), cause);
                throw new BusinessException("库存服务不可用，确认库存失败");
            }
            @Override
            public ApiResponse<Void> rollback(Long orderId, List<StockDeductCmd.Item> items) {
                log.error("[降级] stockFeign.confirm 失败: {}", cause.toString(), cause);
                throw new BusinessException("库存服务不可用，回滚失败");
            }
        };
    }
}*/
