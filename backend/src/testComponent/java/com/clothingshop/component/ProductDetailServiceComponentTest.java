package com.clothingshop.component;

import com.clothingshop.entity.Category;
import com.clothingshop.entity.ImageVariant;
import com.clothingshop.entity.Product;
import com.clothingshop.entity.ProductImage;
import com.clothingshop.entity.ProductPrice;
import com.clothingshop.exception.ResourceNotFoundException;
import com.clothingshop.model.ProductDetailResponse;
import com.clothingshop.model.ProductImageDto;
import com.clothingshop.model.ProductPriceDto;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers(disabledWithoutDocker = true)
@Tag("component")
class ProductDetailServiceComponentTest extends AbstractComponentTest {

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

    @BeforeEach
    void cleanUp() {
        productImageRepository.deleteAll();
        productPriceRepository.deleteAll();
        productRepository.deleteAll();
        List<Category> categories = categoryRepository.findAll();
        coatsCategory = categories.stream().filter(c -> c.getSlug().equals("coats")).findFirst().orElseThrow();
    }

    @Test
    void getProductDetail_returnsFullDetail() {
        Product product = createProduct("Test Coat", coatsCategory, true,
                "100% cotton", "Machine wash cold",
                "Poland", "Organic cotton");

        ProductDetailResponse response = productService.getProductDetail(product.getId(), "PLN");

        assertEquals(product.getId(), response.getId());
        assertEquals("Test Coat", response.getName());
        assertEquals("Full description of Test Coat", response.getDescription());
        assertEquals("Short: Test Coat", response.getShortDescription());
        assertEquals(0, BigDecimal.valueOf(149.99).compareTo(response.getPrice()));
        assertEquals("PLN", response.getCurrency());
        assertNotNull(response.getCategory());
        assertEquals("coats", response.getCategory().getSlug());
        assertEquals("Coats", response.getCategory().getName());
        assertNotNull(response.getFabrication());
        assertEquals("100% cotton", response.getFabrication().getContent());
        assertEquals("Machine wash cold", response.getFabrication().getCare());
        assertNotNull(response.getEthics());
        assertEquals("Poland", response.getEthics().getOrigin());
        assertEquals("Organic cotton", response.getEthics().getImpact());
    }

    @Test
    void getProductDetail_withImages_returnsOrderedImages() {
        Product product = createProduct("Test Coat", coatsCategory, true);
        createImage(product, "products/test/img3.jpg", "Back view", 2);
        createImage(product, "products/test/img1.jpg", "Front view", 0);
        createImage(product, "products/test/img2.jpg", "Side view", 1);

        ProductDetailResponse response = productService.getProductDetail(product.getId(), "PLN");

        List<ProductImageDto> images = response.getImages();
        assertEquals(3, images.size());

        // Ordered by display_order ascending
        assertEquals(0, images.get(0).getDisplayOrder());
        assertEquals("Front view", images.get(0).getAlt());
        assertEquals(1, images.get(1).getDisplayOrder());
        assertEquals("Side view", images.get(1).getAlt());
        assertEquals(2, images.get(2).getDisplayOrder());
        assertEquals("Back view", images.get(2).getAlt());

        // imageUrl should be relative path with /images/ prefix
        assertNotNull(images.get(0).getImageUrl());
        assertTrue(images.get(0).getImageUrl().toString().startsWith("/images/"));
    }

    @Test
    void getProductDetail_primaryImage_isLowestDisplayOrder() {
        Product product = createProduct("Test Coat", coatsCategory, true);
        createImage(product, "products/test/second.jpg", null, 1);
        createImage(product, "products/test/primary.jpg", "Main", 0);

        ProductDetailResponse response = productService.getProductDetail(product.getId(), "PLN");

        assertNotNull(response.getImageUrl());
        assertTrue(response.getImageUrl().toString().contains("primary.jpg"));
    }

    @Test
    void getProductDetail_noImages_imageUrlIsNull() {
        Product product = createProduct("Test Coat", coatsCategory, true);

        ProductDetailResponse response = productService.getProductDetail(product.getId(), "PLN");

        assertNull(response.getImageUrl());
        assertTrue(response.getImages().isEmpty());
    }

    @Test
    void getProductDetail_nonExistentId_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductDetail(UUID.randomUUID(), "PLN"));
    }

    @Test
    void getProductDetail_softDeleted_throwsNotFound() {
        Product product = createProduct("Deleted Coat", coatsCategory, false);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductDetail(product.getId(), "PLN"));
    }

    @Test
    void getProductDetail_noFabricationOrEthics_fieldsAreNull() {
        Product product = createProduct("Basic Coat", coatsCategory, true);

        ProductDetailResponse response = productService.getProductDetail(product.getId(), "PLN");

        assertNull(response.getFabrication());
        assertNull(response.getEthics());
    }

    private Product createProduct(String name, Category category, boolean isActive) {
        return createProduct(name, category, isActive, null, null, null, null);
    }

    private Product createProduct(String name, Category category, boolean isActive,
                                   String fabContent, String fabCare, String ethOrigin, String ethImpact) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Full description of " + name);
        product.setShortDescription("Short: " + name);
        product.setCategory(category);
        product.setIsActive(isActive);
        product.setFabricationContent(fabContent);
        product.setFabricationCare(fabCare);
        product.setEthicsOrigin(ethOrigin);
        product.setEthicsImpact(ethImpact);
        product = productRepository.save(product);
        productPriceRepository.save(new ProductPrice(null, product, "PLN", BigDecimal.valueOf(149.99)));
        productPriceRepository.save(new ProductPrice(null, product, "EUR", BigDecimal.valueOf(34.99)));
        return product;
    }

    private ProductImage createImage(Product product, String objectKey, String alt, int displayOrder) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setObjectKey(objectKey);
        image.setVariant(ImageVariant.ORIGINAL.name());
        image.setAlt(alt);
        image.setDisplayOrder(displayOrder);
        return productImageRepository.save(image);
    }
}
