package com.clothingshop.component;

import com.clothingshop.entity.Category;
import com.clothingshop.entity.Product;
import com.clothingshop.entity.ProductPrice;
import com.clothingshop.model.ProductListResponse;
import com.clothingshop.model.ProductSummary;
import com.clothingshop.repository.CategoryRepository;
import com.clothingshop.repository.ProductImageRepository;
import com.clothingshop.repository.ProductPriceRepository;
import com.clothingshop.repository.ProductRepository;
import com.clothingshop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
@Tag("component")
class ProductListServiceComponentTest extends AbstractComponentTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductPriceRepository productPriceRepository;

    private Category coatsCategory;
    private Category topsCategory;

    @BeforeEach
    void cleanUp() {
        productImageRepository.deleteAll();
        productPriceRepository.deleteAll();
        productRepository.deleteAll();
        List<Category> categories = categoryRepository.findAll();
        coatsCategory = categories.stream().filter(c -> c.getSlug().equals("coats")).findFirst().orElseThrow();
        topsCategory = categories.stream().filter(c -> c.getSlug().equals("tops")).findFirst().orElseThrow();
    }

    @Test
    void listProducts_firstPage_returnsDefaultLimit() {
        for (int i = 0; i < 25; i++) {
            createProduct("Product " + i, coatsCategory, true);
        }

        ProductListResponse response = productService.listProducts(null, null, null, "PLN");

        assertEquals(20, response.getItems().size());
        assertTrue(response.getHasMore());
        assertNotNull(response.getNextCursor());
    }

    @Test
    void listProducts_customLimit() {
        for (int i = 0; i < 10; i++) {
            createProduct("Product " + i, coatsCategory, true);
        }

        ProductListResponse response = productService.listProducts(null, 5, null, "PLN");

        assertEquals(5, response.getItems().size());
        assertTrue(response.getHasMore());
    }

    @Test
    void listProducts_limitCappedAt100() {
        for (int i = 0; i < 5; i++) {
            createProduct("Product " + i, coatsCategory, true);
        }

        ProductListResponse response = productService.listProducts(null, 200, null, "PLN");

        assertEquals(5, response.getItems().size());
        assertFalse(response.getHasMore());
    }

    @Test
    void listProducts_paginationWithCursor() {
        for (int i = 0; i < 7; i++) {
            createProduct("Product " + i, coatsCategory, true);
        }

        // First page
        ProductListResponse page1 = productService.listProducts(null, 3, null, "PLN");
        assertEquals(3, page1.getItems().size());
        assertTrue(page1.getHasMore());
        assertNotNull(page1.getNextCursor());

        // Second page
        ProductListResponse page2 = productService.listProducts(page1.getNextCursor(), 3, null, "PLN");
        assertEquals(3, page2.getItems().size());
        assertTrue(page2.getHasMore());

        // Third page (last)
        ProductListResponse page3 = productService.listProducts(page2.getNextCursor(), 3, null, "PLN");
        assertEquals(1, page3.getItems().size());
        assertFalse(page3.getHasMore());
        assertNull(page3.getNextCursor());
    }

    @Test
    void listProducts_filterByCategory() {
        createProduct("Coat", coatsCategory, true);
        createProduct("Hoodie", topsCategory, true);

        ProductListResponse response = productService.listProducts(null, null, "coats", "PLN");

        assertEquals(1, response.getItems().size());
        assertEquals("Coat", response.getItems().get(0).getName());
    }

    @Test
    void listProducts_emptyDatabase() {
        ProductListResponse response = productService.listProducts(null, null, null, "PLN");

        assertTrue(response.getItems().isEmpty());
        assertFalse(response.getHasMore());
        assertNull(response.getNextCursor());
    }

    @Test
    void listProducts_invalidCursor_throwsBadRequest() {
        assertThrows(Exception.class, () -> productService.listProducts("invalid!!!cursor", null, null, "PLN"));
    }

    @Test
    void listProducts_cursorEncodingRoundTrip() {
        for (int i = 0; i < 5; i++) {
            createProduct("Product " + i, coatsCategory, true);
        }

        ProductListResponse page1 = productService.listProducts(null, 2, null, "PLN");
        String cursor = page1.getNextCursor();
        assertNotNull(cursor);

        // Cursor should be valid base64
        assertDoesNotThrow(() -> java.util.Base64.getUrlDecoder().decode(cursor));

        // Using cursor should return next page
        ProductListResponse page2 = productService.listProducts(cursor, 2, null, "PLN");
        assertEquals(2, page2.getItems().size());
    }

    @Test
    void listProducts_nonExistentCategory_returnsEmpty() {
        createProduct("Coat", coatsCategory, true);

        ProductListResponse response = productService.listProducts(null, null, "nonexistent", "PLN");

        assertTrue(response.getItems().isEmpty());
        assertFalse(response.getHasMore());
    }

    private Product createProduct(String name, Category category, boolean isActive) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Description of " + name);
        product.setShortDescription("Short: " + name);
        product.setCategory(category);
        product.setIsActive(isActive);
        product = productRepository.save(product);
        productPriceRepository.save(new ProductPrice(null, product, "PLN", BigDecimal.valueOf(99.99)));
        productPriceRepository.save(new ProductPrice(null, product, "EUR", BigDecimal.valueOf(22.99)));
        return product;
    }
}
