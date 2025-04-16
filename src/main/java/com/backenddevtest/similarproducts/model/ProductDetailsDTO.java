package com.backenddevtest.similarproducts.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class ProductDetailsDTO {

    private String id;
    private String name;
    private Double price;
    private Boolean availability;
    @JsonIgnore
    private Boolean fallback = false;


}
