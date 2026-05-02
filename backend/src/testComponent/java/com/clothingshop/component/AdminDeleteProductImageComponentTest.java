package com.clothingshop.component;

import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ImageUploadResponse;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminDeleteProductImageComponentTest extends AbstractComponentTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID categoryId;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM product_image");
        jdbcTemplate.update("DELETE FROM product_price");
        jdbcTemplate.update("DELETE FROM product");
        categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM category ORDER BY display_order LIMIT 1", UUID.class);
    }

    @Test
    void deleteProductImage_success_returns204() {
        UUID productId = createProduct();
        UUID imageId = uploadImage(productId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}/images/{imageId}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                productId, imageId
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify image is deleted from DB
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_image WHERE id = ?", Integer.class, imageId);
        assertEquals(0, count);
    }

    @Test
    void deleteProductImage_notFound_returns404() {
        UUID productId = createProduct();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}/images/{imageId}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                productId, UUID.randomUUID()
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteProductImage_unauthorized_returns401() {
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}/images/{imageId}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                UUID.randomUUID(), UUID.randomUUID()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private UUID createProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Test Product");
        request.setDescription("Test description");
        request.setShortDescription("Test short desc");
        request.setPrices(List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(29.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(6.99))
        ));
        request.setCategoryId(categoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        HttpEntity<CreateProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/v1/admin/products",
                HttpMethod.POST,
                entity,
                ProductResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody().getId();
    }

    private UUID uploadImage(UUID productId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth("admin", "admin");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01}) {
            @Override
            public String getFilename() {
                return "test.jpg";
            }
        });
        body.add("alt", "Test image");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ImageUploadResponse> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}/image",
                HttpMethod.POST,
                entity,
                ImageUploadResponse.class,
                productId
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody().getImageId();
    }
}
