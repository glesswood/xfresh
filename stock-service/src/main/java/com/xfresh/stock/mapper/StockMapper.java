package com.xfresh.stock.mapper;

import com.xfresh.dto.StockDTO;
import com.xfresh.stock.entity.Stock;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    StockDTO toDto(Stock entity);

    List<StockDTO> toDtoList(List<Stock> list);
}