package com.backenddevtest.similarproducts.unitTest.controller;

import com.backenddevtest.similarproducts.controler.ProductController;
import com.backenddevtest.similarproducts.model.ProductDetailsDTO;
import com.backenddevtest.similarproducts.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void getSimilarProducts_ProductNotExist_Throws404() {

        String productId = "1";
        when(productService.productExists(productId)).thenReturn(true); // true = product NOT found

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> productController.getSimilarProducts(productId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(productService, times(1)).productExists(productId);
    }

    @Test
    void getSimilarProducts_ProductExists_ReturnsProducts() {

        String productId = "2";
        
        ProductDetailsDTO product1 = new ProductDetailsDTO();
        product1.setId("3");
        product1.setName("Product 3");
        product1.setPrice(30.0);
        product1.setFallback(true);

        ProductDetailsDTO product2 = new ProductDetailsDTO();
        product2.setId("4");
        product2.setName("Product 4");
        product2.setPrice(40.0);
        product2.setFallback(true);

        List<ProductDetailsDTO> mockProducts = List.of(product1, product2);

        when(productService.productExists(productId)).thenReturn(false); 
        when(productService.getSimilarProductsDetailsList(productId))
            .thenReturn(ResponseEntity.ok(mockProducts));

        ResponseEntity<List<ProductDetailsDTO>> response = 
            productController.getSimilarProducts(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProducts, response.getBody());
        verify(productService, times(1)).getSimilarProductsDetailsList(productId);
    }

    @Test
    void getSimilarProducts_ProductExistsButNoSimilar_ReturnsEmptyList() {

        String productId = "5";
        when(productService.productExists(productId)).thenReturn(false);
        when(productService.getSimilarProductsDetailsList(productId))
            .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        ResponseEntity<List<ProductDetailsDTO>> response = 
            productController.getSimilarProducts(productId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}