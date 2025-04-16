package com.backenddevtest.similarproducts.utils;

import com.backenddevtest.similarproducts.model.ProductDetailsDTO;

public final class SimilarProductsUtils {

    private SimilarProductsUtils(){
        throw new UnsupportedOperationException("THIS IS A UTILITY CLASS CANNOT BE INSTANTIATED");
    }

    
    public static ProductDetailsDTO createFallBack() {
        ProductDetailsDTO productDetails = new ProductDetailsDTO();
        productDetails.setFallback(true);
        return productDetails;
    }

    public static boolean isFallback(ProductDetailsDTO productDetails) {
        return productDetails != null && productDetails.getFallback();
    }
    
}
