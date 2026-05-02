package com.clothingshop.controller;

import com.clothingshop.api.CategoriesApi;
import com.clothingshop.model.CategoryDto;
import com.clothingshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CategoryController implements CategoriesApi {

    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<List<CategoryDto>> listCategories() {
        List<CategoryDto> categories = productService.listCategories();
        return ResponseEntity.ok(categories);
    }
}
