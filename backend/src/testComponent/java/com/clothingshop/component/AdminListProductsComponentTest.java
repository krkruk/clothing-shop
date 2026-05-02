package com.clothingshop.component;

import com.clothingshop.model.AdminProductListResponse;
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

class AdminListProductsComponentTest extends AbstractComponentTest {

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
    void listAdminProducts_returnsAllProductsIncludingInactive() {
        createProduct("Active Product", true);
        createProduct("Inactive Product", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<AdminProductListResponse> response = restTemplate.exchange(
                "/api/v1/admin/products",
                HttpMethod.GET,
                entity,
                AdminProductListResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getItems().size());
    }

    @Test
    void listAdminProducts_unauthorized_returns401() {
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/products",
                HttpMethod.GET,
                entity,
                Void.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void listAdminProducts_pagination_returnsCorrectPages() {
        for (int i = 0; i < 5; i++) {
            createProduct("Product " + i, true);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // First page
        ResponseEntity<AdminProductListResponse> response = restTemplate.exchange(
                "/api/v1/admin/products?limit=3",
                HttpMethod.GET,
                entity,
                AdminProductListResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getItems().size());
        assertTrue(response.getBody().getHasMore());
        assertNotNull(response.getBody().getNextCursor());

        // Second page
        String cursor = response.getBody().getNextCursor();
        ResponseEntity<AdminProductListResponse> response2 = restTemplate.exchange(
                "/api/v1/admin/products?limit=3&cursor=" + cursor,
                HttpMethod.GET,
                entity,
                AdminProductListResponse.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(2, response2.getBody().getItems().size());
        assertFalse(response2.getBody().getHasMore());
    }

    @Test
    void listAdminProducts_emptyDatabase_returnsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<AdminProductListResponse> response = restTemplate.exchange(
                "/api/v1/admin/products",
                HttpMethod.GET,
                entity,
                AdminProductListResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getItems().isEmpty());
        assertFalse(response.getBody().getHasMore());
    }

    private UUID createProduct(String name, boolean isActive) {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription("Description of " + name);
        request.setShortDescription("Short: " + name);
        request.setPrices(List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(99.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(22.99))
        ));
        request.setCategoryId(categoryId);
        request.setIsActive(isActive);

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
