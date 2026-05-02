package com.clothingshop.component;

import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ImageUploadResponse;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
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

class ImageUploadComponentTest extends AbstractComponentTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void uploadImage_endToEnd_returns201() {
        UUID productId = createProduct();

        // Upload image
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
        body.add("alt", "Test product image");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ImageUploadResponse> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}/image",
                HttpMethod.POST,
                entity,
                ImageUploadResponse.class,
                productId
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getImageId());

        // Verify product_image record in database via object_key
        String expectedKeyPrefix = "products/" + productId + "/";
        String objectKey = jdbcTemplate.queryForObject(
                "SELECT object_key FROM product_image WHERE object_key LIKE ?",
                String.class, expectedKeyPrefix + "%");
        assertNotNull(objectKey);
        assertTrue(objectKey.endsWith("original.jpg"));
    }

    @Test
    void uploadImage_nonExistentProduct_returns404() {
        UUID fakeProductId = UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth("admin", "admin");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override
            public String getFilename() {
                return "test.jpg";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}/image",
                HttpMethod.POST,
                entity,
                Void.class,
                fakeProductId
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadImage_exceeds5MB_returnsError() {
        UUID productId = createProduct();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth("admin", "admin");

        // Create a 6MB file
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(largeContent) {
            @Override
            public String getFilename() {
                return "large.jpg";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    "/api/v1/admin/products/{id}/image",
                    HttpMethod.POST,
                    entity,
                    Void.class,
                    productId
            );
            // If we got a response, it should be an error
            assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError(),
                    "Expected error status for file exceeding 5MB, got: " + response.getStatusCode());
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Connection may be reset by server when rejecting oversized upload
            // This is also acceptable behavior
            assertTrue(true, "Server rejected the oversized upload by closing the connection");
        }
    }

    private UUID createProduct() {
        UUID categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM category ORDER BY display_order LIMIT 1", UUID.class);

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
}
