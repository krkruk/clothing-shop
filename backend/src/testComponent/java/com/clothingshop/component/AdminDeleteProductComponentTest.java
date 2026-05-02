package com.clothingshop.component;

import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AdminDeleteProductComponentTest extends AbstractComponentTest {

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
    void deleteProduct_success_returns204() {
        UUID productId = createProduct("To Delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                productId
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify soft delete in DB
        Boolean isActive = jdbcTemplate.queryForObject(
                "SELECT is_active FROM product WHERE id = ?", Boolean.class, productId);
        assertFalse(isActive);
    }

    @Test
    void deleteProduct_notFound_returns404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                UUID.randomUUID()
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteProduct_unauthorized_returns401() {
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                UUID.randomUUID()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteProduct_idempotent_canDeleteTwice() {
        UUID productId = createProduct("To Delete");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // First delete
        ResponseEntity<Void> response1 = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                productId
        );
        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());

        // Second delete (idempotent)
        ResponseEntity<Void> response2 = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                productId
        );
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
    }

    private UUID createProduct(String name) {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription("Description of " + name);
        request.setShortDescription("Short: " + name);
        request.setPrices(List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(99.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(22.99))
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
