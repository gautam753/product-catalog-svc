package com.mcart.productcatalogsvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
@OpenAPIDefinition(
	    info = @Info(
	        title = "MCart Product Catalog Service API",
	        version = "1.0",
	        description = "Reactive APIs for products, categories, variants, and filters"
	    )
	)
@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "com.mcart.productcatalogsvc.repository")
public class ProductCatalogSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductCatalogSvcApplication.class, args);
	}

}