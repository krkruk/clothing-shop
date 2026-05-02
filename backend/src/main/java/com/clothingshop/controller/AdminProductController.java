package com.clothingshop.controller;

import com.clothingshop.api.AdminProductsApi;
import com.clothingshop.model.AdminProductListResponse;
import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ImageUploadResponse;
import com.clothingshop.model.ProductResponse;
import com.clothingshop.model.UpdateProductRequest;
import com.clothingshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AdminProductController implements AdminProductsApi {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<ProductResponse> createProduct(CreateProductRequest createProductRequest) {
        ProductResponse response = productService.createProduct(createProductRequest);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(UUID id, UpdateProductRequest updateProductRequest) {
        ProductResponse response = productService.updateProduct(id, updateProductRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AdminProductListResponse> listAdminProducts(String cursor, Integer limit) {
        AdminProductListResponse response = productService.listAdminProducts(cursor, limit);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteProductImage(UUID id, UUID imageId) {
        productService.deleteProductImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ImageUploadResponse> uploadProductImage(UUID id, MultipartFile file, String alt) {
        String contentType = file.getContentType();
        ImageUploadResponse response = productService.uploadProductImage(id, file, contentType, alt);
        return ResponseEntity.status(201).body(response);
    }
}
