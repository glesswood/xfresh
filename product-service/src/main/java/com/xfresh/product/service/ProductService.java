// product/service/ProductService.java
package com.xfresh.product.service;

import com.xfresh.product.dto.*;

import java.util.List;

public interface ProductService {
    ProductDTO create(ProductCreateCmd cmd);
    ProductDTO update(ProductUpdateCmd cmd);
    ProductDTO get(Long id);
    List<ProductDTO> list();
    void delete(Long id);
}