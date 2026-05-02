package com.clothingshop.controller;

import com.clothingshop.api.ProductsApi;
import com.clothingshop.model.ProductDetailResponse;
import com.clothingshop.model.ProductListResponse;
import com.clothingshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PublicProductController implements ProductsApi {

    private final ProductService productService;

    public PublicProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<ProductListResponse> listProducts(String cursor, Integer limit, String category, String xCurrencyCode) {
        ProductListResponse response = productService.listProducts(cursor, limit, category, xCurrencyCode);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductDetailResponse> getProductDetail(UUID id, String xCurrencyCode) {
        ProductDetailResponse response = productService.getProductDetail(id, xCurrencyCode);
        return ResponseEntity.ok(response);
    }
}
