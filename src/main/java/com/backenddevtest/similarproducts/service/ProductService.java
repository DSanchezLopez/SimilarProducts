package com.backenddevtest.similarproducts.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.backenddevtest.similarproducts.model.ProductDetailsDTO;

public interface ProductService {

    List<String> getSimilarProductIds(String productId);

    List<ProductDetailsDTO> getProductsByIds(List<String> productIds);

    Boolean productExists(String productId);

    ResponseEntity<List<ProductDetailsDTO>>  getSimilarProductsDetailsList(String productId);

}
