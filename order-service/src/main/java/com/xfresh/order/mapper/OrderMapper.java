package com.xfresh.order.mapper;

import com.xfresh.dto.OrderDTO;
import com.xfresh.dto.cmd.OrderCreateCmd;
import com.xfresh.order.domain.entity.Order;
import com.xfresh.order.domain.entity.OrderItem;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /* ---------- Cmd → Entity ---------- */
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "orderNo",     ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createTime",  ignore = true)
    @Mapping(target = "updateTime",  ignore = true)
    Order toEntity(OrderCreateCmd cmd);

    /* ---------- Entity → DTO ---------- */
    OrderDTO toDto(Order order);

    /* ---------- 把 Cmd 中的明细转出来 ---------- */
    default List<OrderItem> toItems(List<OrderCreateCmd.ItemCmd> items, Order parent) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream()
                .map(i -> {
                    OrderItem oi = new OrderItem();
                    oi.setProductId(i.getProductId());
                    oi.setQuantity(i.getQuantity());
                    oi.setOrder(parent);          // 关键：反向关联，写入 order_id
                    return oi;
                })
                .toList();
    }

    /* after-mapping：挂回父对象 */
    @AfterMapping
    default void linkItems(@MappingTarget Order order, OrderCreateCmd cmd) {
        order.setItems(toItems(cmd.getItems(), order));
    }
}