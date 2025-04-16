package com.backenddevtest.similarproducts.controler;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.backenddevtest.similarproducts.model.ProductDetailsDTO;
import com.backenddevtest.similarproducts.service.ProductService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductDetailsDTO>> getSimilarProducts(@PathVariable String productId) {
        
        log.info("GETSIMILARPRODUCTS CALLED WITH ID: {}",productId);
        if (productService.productExists(productId)) {
            log.error("GETSIMILARPRODUCTS ERROR, NOT FOUND OBJECT WITH ID : {}",productId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PRODUCT NOT FOUND");
            
        }


        return productService.getSimilarProductsDetailsList(productId);
    }

}
