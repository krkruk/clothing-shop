package com.clothingshop.component;

import com.clothingshop.entity.Category;
import com.clothingshop.entity.Product;
import com.clothingshop.entity.ProductPrice;
import com.clothingshop.repository.CategoryRepository;
import com.clothingshop.repository.ProductImageRepository;
import com.clothingshop.repository.ProductPriceRepository;
import com.clothingshop.repository.ProductRepository;
import com.clothingshop.repository.projection.ProductSummaryProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
@Tag("component")
class ProductRepositoryComponentTest extends AbstractComponentTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductPriceRepository productPriceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void findProductSummaries_returnsActiveProducts() {
        createProduct("Coat A", coatsCategory, true);
        createProduct("Coat B", coatsCategory, true);

        List<ProductSummaryProjection> results = productRepository.findProductSummaries(
                null, null, null, 20
        );

        assertEquals(2, results.size());
        assertEquals("Coat B", results.get(0).getName()); // newest first
    }

    @Test
    void findProductSummaries_excludesSoftDeleted() {
        createProduct("Active", coatsCategory, true);
        createProduct("Deleted", coatsCategory, false);

        List<ProductSummaryProjection> results = productRepository.findProductSummaries(
                null, null, null, 20
        );

        assertEquals(1, results.size());
        assertEquals("Active", results.get(0).getName());
    }

    @Test
    void findProductSummaries_filtersByCategory() {
        createProduct("Coat", coatsCategory, true);
        createProduct("Hoodie", topsCategory, true);

        List<ProductSummaryProjection> results = productRepository.findProductSummaries(
                "coats", null, null, 20
        );

        assertEquals(1, results.size());
        assertEquals("Coat", results.get(0).getName());
    }

    @Test
    void findProductSummaries_nonExistentCategory_returnsEmpty() {
        createProduct("Coat", coatsCategory, true);

        List<ProductSummaryProjection> results = productRepository.findProductSummaries(
                "nonexistent", null, null, 20
        );

        assertTrue(results.isEmpty());
    }

    @Test
    void findProductSummaries_respectsPageSize() {
        for (int i = 0; i < 5; i++) {
            createProduct("Product " + i, coatsCategory, true);
        }

        List<ProductSummaryProjection> results = productRepository.findProductSummaries(
                null, null, null, 3
        );

        assertEquals(3, results.size());
    }

    @Test
    void findProductSummaries_paginationWithCursor() {
        Product first = createProduct("First", coatsCategory, true);
        Product second = createProduct("Second", coatsCategory, true);
        Product third = createProduct("Third", coatsCategory, true);

        // Get first page
        List<ProductSummaryProjection> page1 = productRepository.findProductSummaries(
                null, null, null, 2
        );
        assertEquals(2, page1.size());

        // Use cursor from last item of page 1
        ProductSummaryProjection lastOfPage1 = page1.get(page1.size() - 1);
        List<ProductSummaryProjection> page2 = productRepository.findProductSummaries(
                null, lastOfPage1.getCreatedAt(), lastOfPage1.getId(), 2
        );
        assertEquals(1, page2.size());
    }

    @Test
    void findProductSummaries_includesCategoryInfo() {
        createProduct("Coat", coatsCategory, true);

        List<ProductSummaryProjection> results = productRepository.findProductSummaries(
                null, null, null, 20
        );

        assertEquals(1, results.size());
        assertEquals("coats", results.get(0).getCategorySlug());
        assertEquals("Coats", results.get(0).getCategoryName());
    }

    @Test
    void findByIdAndIsActiveTrue_returnsActiveOnly() {
        Product active = createProduct("Active", coatsCategory, true);
        Product deleted = createProduct("Deleted", coatsCategory, false);

        assertTrue(productRepository.findByIdAndIsActiveTrue(active.getId()).isPresent());
        assertTrue(productRepository.findByIdAndIsActiveTrue(deleted.getId()).isEmpty());
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
