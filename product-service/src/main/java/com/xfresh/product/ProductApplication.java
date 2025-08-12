package com.xfresh.product;

import com.xfresh.product.entity.Product;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import com.xfresh.product.repository.ProductRepository;
@SpringBootApplication(scanBasePackages = "com.xfresh")

@OpenAPIDefinition(info =
	@Info(title = "Product API", version = "1.0", description = "Documentation Product API v1.0")
)
@EnableFeignClients
public class ProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApplication.class, args);
	}
	
}
