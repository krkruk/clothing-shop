package com.clothingshop.component;

import com.clothingshop.model.AdminProductListResponse;
import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import com.clothingshop.model.UpdateProductRequest;
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

class AdminUpdateProductComponentTest extends AbstractComponentTest {

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
    void updateProduct_success_returns200() {
        UUID productId = createProduct("Original Name");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Name");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        HttpEntity<UpdateProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.PUT,
                entity,
                ProductResponse.class,
                productId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());

        // Verify DB
        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM product WHERE id = ?", String.class, productId);
        assertEquals("Updated Name", name);
    }

    @Test
    void updateProduct_notFound_returns404() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Name");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        HttpEntity<UpdateProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.PUT,
                entity,
                Void.class,
                UUID.randomUUID()
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateProduct_unauthorized_returns401() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Updated Name");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<UpdateProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.PUT,
                entity,
                Void.class,
                UUID.randomUUID()
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateProduct_partialUpdate_preservesOtherFields() {
        UUID productId = createProduct("Original Name");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setPrices(List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(199.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(46.99))
        ));
        // Name is not set - should remain "Original Name"

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        HttpEntity<UpdateProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.PUT,
                entity,
                ProductResponse.class,
                productId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Original Name", response.getBody().getName());
        BigDecimal plnPrice = response.getBody().getPrices().stream()
                .filter(p -> "PLN".equals(p.getCurrency())).findFirst()
                .map(ProductPriceDto::getPrice).orElse(null);
        assertEquals(0, BigDecimal.valueOf(199.99).compareTo(plnPrice));
    }

    @Test
    void updateProduct_duplicateSku_returns422() {
        UUID product1Id = createProductWithSku("Product 1", "SKU-001");
        createProductWithSku("Product 2", "SKU-002");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setSku("SKU-002"); // Duplicate

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("admin", "admin");

        HttpEntity<UpdateProductRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products/{id}",
                HttpMethod.PUT,
                entity,
                Void.class,
                product1Id
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    private UUID createProduct(String name) {
        return createProductWithSku(name, null);
    }

    private UUID createProductWithSku(String name, String sku) {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription("Description of " + name);
        request.setShortDescription("Short: " + name);
        request.setPrices(List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(99.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(22.99))
        ));
        request.setCategoryId(categoryId);
        if (sku != null) {
            request.setSku(sku);
        }

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
