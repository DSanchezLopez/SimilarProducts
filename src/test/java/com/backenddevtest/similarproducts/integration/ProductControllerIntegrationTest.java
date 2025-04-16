package com.backenddevtest.similarproducts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import com.backenddevtest.similarproducts.service.ProductService;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "product-service.product-details-url=http://testhost/product/{productId}",
    "product-service.product-similar-url=http://testhost/product/{productId}/similar"
})

public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = (RestTemplate) ReflectionTestUtils.getField(productService, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    void getSimilarProducts_ProductNotFound_Returns404() throws Exception {

        mockServer.expect(requestTo("http://testhost/product/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/product/999/similar"))
                .andExpect(status().isNotFound());

        mockServer.verify();
    }

    @Test
    void getSimilarProducts_SimilarIdsEmpty_ReturnsEmptyList() throws Exception {

        mockServer.expect(requestTo("http://testhost/product/4"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{ \"id\": \"4\", \"name\": \"Product 4\", \"price\": 400.0, \"fallback\": false }",
                        MediaType.APPLICATION_JSON
                ));

        mockServer.expect(requestTo("http://testhost/product/4/similar"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/product/4/similar"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));

        mockServer.verify();
    }

}