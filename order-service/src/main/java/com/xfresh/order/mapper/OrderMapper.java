package com.xfresh.order.mapper;

import com.xfresh.order.dto.OrderDTO;
import com.xfresh.order.dto.OrderItemDTO;
import com.xfresh.order.dto.cmd.OrderCreateCmd;
import com.xfresh.order.entity.Order;
import com.xfresh.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct 会在编译期帮我们生成实现类；启用 componentModel=spring 后直接注入即可
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    /* ========== Cmd -> Entity ========== */

    @Mapping(target = "id", ignore = true)           // 主键自增
    @Mapping(target = "orderNo", ignore = true)      // 业务序号后面手动填
    @Mapping(target = "totalAmount", ignore = true)  // 服务层再计算
    @Mapping(target = "status", ignore = true)       // 默认 0
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Order toEntity(OrderCreateCmd cmd);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)        // 多对一由 service 填充
    OrderItem toEntity(OrderCreateCmd.ItemCmd cmd);

    /* ========== Entity -> DTO ========== */

    OrderDTO toDto(Order entity);

    /**
     * MapStruct 能自动把 List<OrderItem> -> List<OrderItemDTO>，
     * 但单个映射方法最好也写上，方便后期复用
     */
    OrderItemDTO toDto(OrderItem entity);

    /* 如果喜欢链式写法，也可以把 List 映射方法显式列出来 */
    List<OrderItemDTO> toItemDtoList(List<OrderItem> list);
}