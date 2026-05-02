package com.clothingshop.component;

import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductComponentTest extends AbstractComponentTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createProduct_endToEnd_returns201() {
        UUID categoryId = getFirstCategoryId();
        assertNotNull(categoryId, "Seeded category should exist");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Obsidian Hoodie");
        request.setDescription("A dark hoodie forged in the depths");
        request.setShortDescription("Dark hoodie");
        request.setPrices(List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(149.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(34.99))
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
        assertNotNull(response.getBody());
        assertEquals("Obsidian Hoodie", response.getBody().getName());
        BigDecimal plnPrice = response.getBody().getPrices().stream()
                .filter(p -> "PLN".equals(p.getCurrency())).findFirst()
                .map(ProductPriceDto::getPrice).orElse(null);
        assertEquals(0, BigDecimal.valueOf(149.99).compareTo(plnPrice));
        assertNotNull(response.getBody().getId());

        // Verify record in database
        UUID productId = response.getBody().getId();
        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM product WHERE id = ?", String.class, productId);
        assertEquals("Obsidian Hoodie", name);
    }

    private UUID getFirstCategoryId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM category ORDER BY display_order LIMIT 1", UUID.class);
    }
}
