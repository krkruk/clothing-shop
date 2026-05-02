package com.clothingshop.component;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CategoryListComponentTest extends AbstractComponentTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void listCategories_returnsOrderedCategories() {
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                "/api/v1/categories",
                HttpMethod.GET,
                entity,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());

        // Verify ordering: tops, coats, bottoms, accessories
        Map<String, Object> first = (Map<String, Object>) response.getBody().get(0);
        assertEquals("tops", first.get("slug"));
        assertEquals("Tops", first.get("name"));

        Map<String, Object> second = (Map<String, Object>) response.getBody().get(1);
        assertEquals("coats", second.get("slug"));

        Map<String, Object> third = (Map<String, Object>) response.getBody().get(2);
        assertEquals("bottoms", third.get("slug"));

        Map<String, Object> fourth = (Map<String, Object>) response.getBody().get(3);
        assertEquals("accessories", fourth.get("slug"));
    }

    @Test
    void listCategories_includesIdField() {
        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                "/api/v1/categories",
                HttpMethod.GET,
                entity,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> first = (Map<String, Object>) response.getBody().get(0);
        assertNotNull(first.get("id"));
    }
}
