// product/mapper/ProductMapper.java
package com.xfresh.product.mapper;

import com.xfresh.product.dto.*;
import com.xfresh.product.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toDto(Product entity);

    Product toEntity(ProductCreateCmd cmd);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProductUpdateCmd cmd, @MappingTarget Product entity);
}