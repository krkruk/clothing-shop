package com.clothingshop.controller;

import com.clothingshop.config.SecurityConfig;
import com.clothingshop.exception.GlobalExceptionHandler;
import com.clothingshop.exception.ResourceNotFoundException;
import com.clothingshop.exception.ValidationException;
import com.clothingshop.model.CategoryDto;
import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ImageUploadResponse;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import com.clothingshop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProductController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";

    private List<ProductPriceDto> buildDefaultPrices() {
        return List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(99.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(23.45))
        );
    }

    // --- Create Product Tests ---

    @Test
    void createProduct_validationErrors_returns422() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        // name is null -> validation error

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createProduct_missingAuth_returns401() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Test");
        request.setDescription("Desc");
        request.setShortDescription("Short");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_success_returns201() throws Exception {
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setSlug("tops");
        categoryDto.setName("Tops");

        ProductResponse response = new ProductResponse();
        response.setId(productId);
        response.setName("Dark Hoodie");
        response.setDescription("A very dark hoodie");
        response.setShortDescription("Dark hoodie");
        response.setPrices(buildDefaultPrices());
        response.setCategory(categoryDto);

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dark Hoodie"))
                .andExpect(jsonPath("$.id").value(productId.toString()));
    }

    @Test
    void createProduct_categoryNotFound_returns422() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(UUID.randomUUID());

        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenThrow(new ValidationException("Category not found"));

        mockMvc.perform(post("/api/v1/admin/products")
                        .with(httpBasic(ADMIN_USER, ADMIN_PASS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    // --- Upload Image Tests ---

    @Test
    void uploadImage_productNotFound_returns404() throws Exception {
        UUID productId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(productService.uploadProductImage(eq(productId), any(), eq("image/jpeg"), any()))
                .thenThrow(new ResourceNotFoundException("Product not found: " + productId));

        mockMvc.perform(multipart("/api/v1/admin/products/{id}/image", productId)
                        .file(file)
                        .with(httpBasic(ADMIN_USER, ADMIN_PASS)))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadImage_success_returns201() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});

        ImageUploadResponse response = new ImageUploadResponse();
        response.setImageId(imageId);
        response.setImageUrl(URI.create("/products/" + productId + "/" + imageId + "/original.jpg"));

        when(productService.uploadProductImage(eq(productId), any(), eq("image/jpeg"), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/admin/products/{id}/image", productId)
                        .file(file)
                        .with(httpBasic(ADMIN_USER, ADMIN_PASS)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(imageId.toString()));
    }
}
