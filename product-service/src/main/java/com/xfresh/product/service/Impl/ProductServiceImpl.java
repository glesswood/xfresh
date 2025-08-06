// product/service/impl/ProductServiceImpl.java
package com.xfresh.product.service.Impl;

import com.xfresh.common.BusinessException;
import com.xfresh.product.dto.*;
import com.xfresh.product.entity.Product;
import com.xfresh.product.mapper.ProductMapper;
import com.xfresh.product.repository.ProductRepository;
import com.xfresh.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    @Override @Transactional
    public ProductDTO create(ProductCreateCmd cmd) {
        Product saved = repo.save(mapper.toEntity(cmd));
        return mapper.toDto(saved);
    }

    @Override @Transactional
    public ProductDTO update(ProductUpdateCmd cmd) {
        Product p = repo.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        mapper.updateEntity(cmd, p);
        return mapper.toDto(p);
    }

    @Override
    public ProductDTO get(Long id) {
        return repo.findById(id).map(mapper::toDto)
                .orElseThrow(() -> new BusinessException("商品不存在"));
    }

    @Override
    public List<ProductDTO> list() {
        return repo.findAll().stream().map(mapper::toDto).toList();
    }

    @Override @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}