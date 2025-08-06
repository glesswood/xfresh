// product/repository/ProductRepository.java
package com.xfresh.product.repository;

import com.xfresh.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}