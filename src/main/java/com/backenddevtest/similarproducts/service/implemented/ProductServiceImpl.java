package com.backenddevtest.similarproducts.service.implemented;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.backenddevtest.similarproducts.model.ProductDetailsDTO;
import com.backenddevtest.similarproducts.service.ProductService;
import com.backenddevtest.similarproducts.utils.SimilarProductsUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Value("${product-service.product-details-url}")
    private String PRODUCT_DETAILS_URL;
    @Value("${product-service.product-similar-url}")
    private String PRODUCTS_SIMILAR_IDS_URL;

    private RestTemplate restTemplate;


    public ProductServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Boolean productExists(String productId) {

        log.info("CHECKING IF THE PRODUCT EXIST WITH ID : {}", productId);

        ProductDetailsDTO productDetails = fetchProductDetailsWithFallBack(productId);

        return productDetails.getFallback();

    }

    @Override
    public List<String> getSimilarProductIds(String productId) {

        log.info("GETTING LIST OF SIMILAR IDS AT ID : {}", productId);

        try {
            ResponseEntity<String[]> response = restTemplate.getForEntity(
                    PRODUCTS_SIMILAR_IDS_URL,
                    String[].class,
                    productId);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return List.of(response.getBody());
            } else {
                log.warn("NON-OK STATUS FOR SIMILAR PRODUCTS IDS {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("SIMILAR PRODUCT IDs NOT FOUND FOR ID {}", productId);
                return Collections.emptyList();
            } else {
                log.error("ERROR GETTING SIMILAR PRODUCTS IDs FOR ID {}, {}", productId, e);
                throw e;
            }
        } catch (RestClientException e) {
            log.error(" REST CLIENT ERROR GETTING SIMILAR PRODUCTS IDs FOR ID", productId, e);
            return Collections.emptyList();
        }

    }

    @Override
    public List<ProductDetailsDTO> getProductsByIds(List<String> productIds) {

        log.info("GETTING DETAILS OF PRODUCTS AT IDs : {}", productIds);

        List<ProductDetailsDTO> resultProductDetails = new ArrayList<>();

        try{
            resultProductDetails= productIds.parallelStream()
                .map(this::fetchProductDetailsWithFallBack)
                .filter(product -> !SimilarProductsUtils.isFallback(product))
                .distinct()
                .collect(Collectors.toList());
        }catch(Exception e){//Poner exception concreta

            log.error("ERROR ON GETTING AND FILTERING OPERATION getProductsByIds {}", e);
            resultProductDetails= new ArrayList<>();
        }

        return resultProductDetails;

    }

    private ProductDetailsDTO fetchProductDetailsWithFallBack(String productId) {

        try {
            ResponseEntity<ProductDetailsDTO> response = restTemplate.getForEntity(
                    PRODUCT_DETAILS_URL,
                    ProductDetailsDTO.class,
                    productId);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {

                log.warn("NON-OK STATUS FOR PRODUCTS DETAILS {}", response.getStatusCode());
                return SimilarProductsUtils.createFallBack();

            }
        } catch (RestClientException e) {

            log.error(" REST CLIENT ERROR GETTING PRODUCT DETAILS FOR ID {} , {}", productId, e.getMessage());
            return SimilarProductsUtils.createFallBack();

        }

        

    }

    @Override
    public ResponseEntity<List<ProductDetailsDTO>>  getSimilarProductsDetailsList(String productId) {

        ResponseEntity<List<ProductDetailsDTO>>  responseEntity;
        List<String> similarProductIds = getSimilarProductIds(productId);

        if (similarProductIds.isEmpty()) {
           log.error("SIMILAR PRODUCTS NOT FOUND TO ID : {}",productId);
           responseEntity= ResponseEntity.ok(Collections.emptyList());
        }else{
            log.info("SIMILAR PRODUCTS FOUND TO ID : {}",productId);
        responseEntity= ResponseEntity.ok(getProductsByIds(similarProductIds));
        }

        return responseEntity;

    }

}
