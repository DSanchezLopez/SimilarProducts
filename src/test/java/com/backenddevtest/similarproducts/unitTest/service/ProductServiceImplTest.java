package com.backenddevtest.similarproducts.unitTest.service;

import com.backenddevtest.similarproducts.model.ProductDetailsDTO;
import com.backenddevtest.similarproducts.service.implemented.ProductServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private ProductServiceImpl productService;

    private final String productDetailsUrl = "http://test.url/product/{productId}";
    private final String similarIdsUrl = "http://test.url/similarIds";

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(restTemplate);
        
        ReflectionTestUtils.setField(productService, "PRODUCT_DETAILS_URL", productDetailsUrl);
        ReflectionTestUtils.setField(productService, "PRODUCTS_SIMILAR_IDS_URL", similarIdsUrl);
    }

    @Test
    void getSimilarProductIds_Success() {
        String productId = "1";
        String[] mockIds = {"2", "3"};
        
        when(restTemplate.getForEntity(similarIdsUrl, String[].class, productId))
            .thenReturn(ResponseEntity.ok(mockIds));

        List<String> result = productService.getSimilarProductIds(productId);
        assertEquals(List.of("2", "3"), result);
    }

    @Test
    void productExists_ProductFound_ReturnsFalse() {
        String productId = "4";
        ProductDetailsDTO product = new ProductDetailsDTO();
        product.setId(productId);
        product.setFallback(false); 

        when(restTemplate.getForEntity(productDetailsUrl, ProductDetailsDTO.class, productId))
            .thenReturn(ResponseEntity.ok(product));

        assertFalse(productService.productExists(productId));
    }

    @Test
    void productExists_ProductNotFound_ReturnsTrue() {
        String productId = "5";
        when(restTemplate.getForEntity(productDetailsUrl, ProductDetailsDTO.class, productId))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertTrue(productService.productExists(productId));
    }

    @Test
    void getProductsByIds_MixedValidAndInvalid() {
        String validId = "6";
        String invalidId = "7";
        
        ProductDetailsDTO validProduct = new ProductDetailsDTO();
        validProduct.setId(validId);
        validProduct.setFallback(false);

        when(restTemplate.getForEntity(productDetailsUrl, ProductDetailsDTO.class, validId))
            .thenReturn(ResponseEntity.ok(validProduct));
        when(restTemplate.getForEntity(productDetailsUrl, ProductDetailsDTO.class, invalidId))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        List<ProductDetailsDTO> result = productService.getProductsByIds(List.of(validId, invalidId));
        assertEquals(1, result.size());
        assertEquals(validProduct, result.get(0));
    }



    @Test
    void getSimilarProductsDetailsList_SimilarIdsServerError_ReturnsEmptyList() {
        String productId = "20";
        when(restTemplate.getForEntity(similarIdsUrl, String[].class, productId))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    
        ResponseEntity<List<ProductDetailsDTO>> response = 
            productService.getSimilarProductsDetailsList(productId);
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
    
    @Test
    void getSimilarProductsDetailsList_SimilarIdsClientError_ReturnsEmptyList() {
        String productId = "21";
        when(restTemplate.getForEntity(similarIdsUrl, String[].class, productId))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    
        ResponseEntity<List<ProductDetailsDTO>> response = 
            productService.getSimilarProductsDetailsList(productId);
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
    
    @Test
    void getSimilarProductsDetailsList_SimilarIdsNullBody_ReturnsEmptyList() {
        String productId = "22";
        when(restTemplate.getForEntity(similarIdsUrl, String[].class, productId))
            .thenReturn(ResponseEntity.ok(null));
    
        ResponseEntity<List<ProductDetailsDTO>> response = 
            productService.getSimilarProductsDetailsList(productId);
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
    
    
}