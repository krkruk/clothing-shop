package com.clothingshop.service;

import com.clothingshop.entity.Category;
import com.clothingshop.entity.Product;
import com.clothingshop.entity.ProductImage;
import com.clothingshop.entity.ProductPrice;
import com.clothingshop.exception.BadRequestException;
import com.clothingshop.exception.ResourceNotFoundException;
import com.clothingshop.exception.ValidationException;
import com.clothingshop.model.AdminProductListResponse;
import com.clothingshop.model.CategoryDto;
import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.FabricationDto;
import com.clothingshop.model.ImageUploadResponse;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import com.clothingshop.model.UpdateProductRequest;
import com.clothingshop.repository.CategoryRepository;
import com.clothingshop.repository.ProductImageRepository;
import com.clothingshop.repository.ProductPriceRepository;
import com.clothingshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.persistence.EntityManager;
import org.mockito.Mock;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductPriceRepository productPriceRepository;
    @Mock
    private S3Client s3Client;
    @Mock
    private EntityManager entityManager;

    private ProductService productService;

    private static final BigDecimal PLN_PRICE = BigDecimal.valueOf(99.99);
    private static final BigDecimal EUR_PRICE = BigDecimal.valueOf(23.45);

    @BeforeEach
    void setUp() throws Exception {
        productService = new ProductService(
                productRepository, categoryRepository, productImageRepository,
                productPriceRepository, s3Client, "clothingshop", new ObjectMapper()
        );
        // Inject @PersistenceContext field via reflection
        Field emField = ProductService.class.getDeclaredField("entityManager");
        emField.setAccessible(true);
        emField.set(productService, entityManager);
    }

    // --- Helper to build a standard two-currency price list ---

    private List<ProductPriceDto> buildDefaultPrices() {
        ProductPriceDto plnDto = new ProductPriceDto("PLN", PLN_PRICE);
        ProductPriceDto eurDto = new ProductPriceDto("EUR", EUR_PRICE);
        return List.of(plnDto, eurDto);
    }

    private List<ProductPrice> buildDefaultProductPrices(Product product) {
        ProductPrice pln = new ProductPrice();
        pln.setProduct(product);
        pln.setCurrency("PLN");
        pln.setPrice(PLN_PRICE);

        ProductPrice eur = new ProductPrice();
        eur.setProduct(product);
        eur.setCurrency("EUR");
        eur.setPrice(EUR_PRICE);

        return List.of(pln, eur);
    }

    private List<ProductPriceDto> pricesToDtos(List<ProductPrice> prices) {
        return prices.stream().map(pp -> new ProductPriceDto(pp.getCurrency(), pp.getPrice())).toList();
    }

    private void assertPricesEqual(List<ProductPriceDto> expected, List<ProductPriceDto> actual) {
        assertEquals(expected.size(), actual.size());
        for (ProductPriceDto expectedPrice : expected) {
            boolean found = actual.stream().anyMatch(a ->
                    a.getCurrency().equals(expectedPrice.getCurrency()) &&
                    a.getPrice().compareTo(expectedPrice.getPrice()) == 0);
            assertTrue(found, "Expected price not found: " + expectedPrice.getCurrency() + " " + expectedPrice.getPrice());
        }
    }

    // --- Create Product Tests ---

    @Test
    void createProduct_happyPath_returnsResponse() {
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            ReflectionTestUtils.setField(p, "id", productId);
            return p;
        });
        when(productPriceRepository.save(any(ProductPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(buildDefaultProductPrices(null));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Dark Hoodie", response.getName());
        assertEquals(productId, response.getId());
        assertPricesEqual(buildDefaultPrices(), response.getPrices());
        assertNotNull(response.getCategory());
        assertEquals("tops", response.getCategory().getSlug());
    }

    @Test
    void createProduct_categoryNotFound_throwsValidationException() {
        UUID categoryId = UUID.randomUUID();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> productService.createProduct(request));
    }

    @Test
    void createProduct_withFabricationAndEthics_mapsCorrectly() {
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);

        FabricationDto fab = new FabricationDto();
        fab.setContent("100% cotton");
        fab.setCare("Machine wash cold");
        request.setFabrication(fab);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            ReflectionTestUtils.setField(p, "id", productId);
            return p;
        });
        when(productPriceRepository.save(any(ProductPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(buildDefaultProductPrices(null));

        ProductResponse response = productService.createProduct(request);
        assertNotNull(response);
    }

    @Test
    void createProduct_duplicateSku_throwsValidationException() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);
        request.setSku("SKU-123");

        Product existingProduct = new Product();
        existingProduct.setSku("SKU-123");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findBySku("SKU-123")).thenReturn(Optional.of(existingProduct));

        assertThrows(ValidationException.class, () -> productService.createProduct(request));
    }

    @Test
    void createProduct_withSkuAndIsActive_mapsCorrectly() {
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);
        request.setSku("SKU-ABC");
        request.setIsActive(false);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findBySku("SKU-ABC")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            ReflectionTestUtils.setField(p, "id", productId);
            return p;
        });
        when(productPriceRepository.save(any(ProductPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(buildDefaultProductPrices(null));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertPricesEqual(buildDefaultPrices(), response.getPrices());
        assertEquals("SKU-ABC", response.getSku());
        assertEquals(false, response.getIsActive());
    }

    @Test
    void createProduct_nullSku_noUniquenessCheck() {
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(buildDefaultPrices());
        request.setCategoryId(categoryId);
        // sku is null by default

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            ReflectionTestUtils.setField(p, "id", productId);
            return p;
        });
        when(productPriceRepository.save(any(ProductPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(buildDefaultProductPrices(null));

        ProductResponse response = productService.createProduct(request);
        assertNotNull(response);
        // findBySku should NOT be called when sku is null
        verify(productRepository, never()).findBySku(anyString());
    }

    @Test
    void createProduct_missingPrices_throwsValidationException() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(List.of()); // empty prices
        request.setCategoryId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThrows(ValidationException.class, () -> productService.createProduct(request));
    }

    @Test
    void createProduct_onlyOneCurrency_throwsValidationException() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Dark Hoodie");
        request.setDescription("A very dark hoodie");
        request.setShortDescription("Dark hoodie");
        request.setPrices(List.of(new ProductPriceDto("PLN", BigDecimal.valueOf(99.99))));
        request.setCategoryId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThrows(ValidationException.class, () -> productService.createProduct(request));
    }

    // --- Update Product Tests ---

    @Test
    void updateProduct_partialUpdate_onlyUpdatesNonNullFields() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(productId);
        product.setName("Old Name");
        product.setDescription("Old Description");
        product.setShortDescription("Old Short");
        product.setSku("OLD-SKU");
        product.setIsActive(true);
        product.setCategory(category);

        List<ProductPrice> existingPrices = buildDefaultProductPrices(product);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");
        request.setPrices(null); // Not updating prices
        // Only updating name - no prices set

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId)).thenReturn(Collections.emptyList());
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(existingPrices);

        ProductResponse response = productService.updateProduct(productId, request);

        assertEquals("New Name", response.getName());
        assertEquals("Old Description", response.getDescription());
        assertPricesEqual(pricesToDtos(existingPrices), response.getPrices());
    }

    @Test
    void updateProduct_skuChange_validatesUniqueness() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(productId);
        product.setName("Name");
        product.setDescription("Desc");
        product.setShortDescription("Short");
        product.setSku("OLD-SKU");
        product.setCategory(category);

        Product otherProduct = new Product();
        otherProduct.setSku("NEW-SKU");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setSku("NEW-SKU");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySku("NEW-SKU")).thenReturn(Optional.of(otherProduct));

        assertThrows(ValidationException.class, () -> productService.updateProduct(productId, request));
    }

    @Test
    void updateProduct_sameSku_noUniquenessError() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(productId);
        product.setName("Name");
        product.setDescription("Desc");
        product.setShortDescription("Short");
        product.setSku("SAME-SKU");
        product.setCategory(category);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setSku("SAME-SKU");
        request.setPrices(null); // Not updating prices

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId)).thenReturn(Collections.emptyList());
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(buildDefaultProductPrices(product));

        ProductResponse response = productService.updateProduct(productId, request);
        assertNotNull(response);
        assertEquals("SAME-SKU", response.getSku());
    }

    @Test
    void updateProduct_notFound_throwsResourceNotFoundException() {
        UUID productId = UUID.randomUUID();

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(productId, request));
    }

    @Test
    void updateProduct_categoryNotFound_throwsValidationException() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(productId);
        product.setName("Name");
        product.setDescription("Desc");
        product.setShortDescription("Short");
        product.setCategory(category);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setCategoryId(newCategoryId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> productService.updateProduct(productId, request));
    }

    @Test
    void updateProduct_fullUpdate_allFieldsUpdated() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Category newCategory = new Category();
        newCategory.setId(newCategoryId);
        category.setSlug("coats");
        newCategory.setNameEn("Coats");

        Product product = new Product();
        product.setId(productId);
        product.setName("Old Name");
        product.setDescription("Old Desc");
        product.setShortDescription("Old Short");
        product.setSku("OLD-SKU");
        product.setIsActive(true);
        product.setCategory(category);

        List<ProductPriceDto> newPrices = List.of(
                new ProductPriceDto("PLN", BigDecimal.valueOf(149.99)),
                new ProductPriceDto("EUR", BigDecimal.valueOf(34.99))
        );

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New Name");
        request.setDescription("New Desc");
        request.setShortDescription("New Short");
        request.setPrices(newPrices);
        request.setSku("NEW-SKU");
        request.setIsActive(false);
        request.setCategoryId(newCategoryId);

        List<ProductPrice> savedPrices = List.of(
                createProductPrice(product, "PLN", BigDecimal.valueOf(149.99)),
                createProductPrice(product, "EUR", BigDecimal.valueOf(34.99))
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySku("NEW-SKU")).thenReturn(Optional.empty());
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId)).thenReturn(Collections.emptyList());
        when(productPriceRepository.save(any(ProductPrice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productPriceRepository.findAllByProductId(productId)).thenReturn(savedPrices);

        ProductResponse response = productService.updateProduct(productId, request);

        assertEquals("New Name", response.getName());
        assertEquals("New Desc", response.getDescription());
        assertEquals("New Short", response.getShortDescription());
        assertPricesEqual(newPrices, response.getPrices());
        assertEquals("NEW-SKU", response.getSku());
        assertEquals(false, response.getIsActive());
    }

    // --- Delete Product Tests ---

    @Test
    void deleteProduct_existingProduct_setsInactive() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(productId);
        product.setName("Name");
        product.setIsActive(true);
        product.setCategory(category);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deleteProduct(productId);

        assertFalse(product.getIsActive());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_alreadyInactive_idempotent() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(productId);
        product.setName("Name");
        product.setIsActive(false);
        product.setCategory(category);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deleteProduct(productId);

        assertFalse(product.getIsActive());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_notFound_throwsResourceNotFoundException() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(productId));
    }

    // --- List Admin Products Tests ---

    @Test
    void listAdminProducts_firstPage_returnsDefaultLimit() {
        // Create 21 products to fill one page + trigger hasMore
        List<Product> products = new java.util.ArrayList<>();
        for (int i = 0; i < 21; i++) {
            Product p = createTestProduct("Product " + i);
            products.add(p);
        }

        when(productRepository.findAllForAdmin(isNull(), isNull(), eq(21))).thenReturn(products);
        when(productPriceRepository.findAllByProductId(any(UUID.class))).thenReturn(buildDefaultProductPrices(null));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(any(UUID.class))).thenReturn(Collections.emptyList());

        AdminProductListResponse response = productService.listAdminProducts(null, null);

        assertEquals(20, response.getItems().size());
        assertTrue(response.getHasMore());
        assertNotNull(response.getNextCursor());
    }

    @Test
    void listAdminProducts_customLimit() {
        List<Product> products = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            products.add(createTestProduct("Product " + i));
        }

        when(productRepository.findAllForAdmin(isNull(), isNull(), eq(6))).thenReturn(products);
        when(productPriceRepository.findAllByProductId(any(UUID.class))).thenReturn(buildDefaultProductPrices(null));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(any(UUID.class))).thenReturn(Collections.emptyList());

        AdminProductListResponse response = productService.listAdminProducts(null, 5);

        assertEquals(5, response.getItems().size());
        assertTrue(response.getHasMore());
    }

    @Test
    void listAdminProducts_emptyDatabase_returnsEmpty() {
        when(productRepository.findAllForAdmin(isNull(), isNull(), eq(21))).thenReturn(Collections.emptyList());

        AdminProductListResponse response = productService.listAdminProducts(null, null);

        assertTrue(response.getItems().isEmpty());
        assertFalse(response.getHasMore());
        assertNull(response.getNextCursor());
    }

    @Test
    void listAdminProducts_includesInactiveProducts() {
        Product active = createTestProduct("Active");
        active.setIsActive(true);
        Product inactive = createTestProduct("Inactive");
        inactive.setIsActive(false);

        // The admin query does not filter by is_active, so both are returned
        when(productRepository.findAllForAdmin(isNull(), isNull(), eq(21))).thenReturn(List.of(active, inactive));
        when(productPriceRepository.findAllByProductId(any(UUID.class))).thenReturn(buildDefaultProductPrices(null));
        when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(any(UUID.class))).thenReturn(Collections.emptyList());

        AdminProductListResponse response = productService.listAdminProducts(null, null);

        assertEquals(2, response.getItems().size());
    }

    @Test
    void listAdminProducts_invalidCursor_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> productService.listAdminProducts("invalid!!!cursor", null));
    }

    // --- Delete Product Image Tests ---

    @Test
    void deleteProductImage_happyPath_deletesImage() {
        UUID productId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        ProductImage image = new ProductImage();
        image.setId(imageId);
        image.setObjectKey("products/" + productId + "/" + imageId + "/original.jpg");

        when(productImageRepository.findByProductIdAndId(productId, imageId)).thenReturn(Optional.of(image));

        productService.deleteProductImage(productId, imageId);

        verify(productImageRepository).delete(image);
        verify(s3Client).deleteObject(any(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
    }

    @Test
    void deleteProductImage_notFound_throwsResourceNotFoundException() {
        UUID productId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        when(productImageRepository.findByProductIdAndId(productId, imageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProductImage(productId, imageId));
    }

    @Test
    void deleteProductImage_wrongProduct_throwsResourceNotFoundException() {
        UUID productId = UUID.randomUUID();
        UUID otherProductId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        when(productImageRepository.findByProductIdAndId(productId, imageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProductImage(productId, imageId));
    }

    // --- List Categories Tests ---

    @Test
    void listCategories_returnsOrderedCategories() {
        Category cat1 = new Category();
        cat1.setId(UUID.randomUUID());
        cat1.setSlug("tops");
        cat1.setNameEn("Tops");
        cat1.setDisplayOrder(1);

        Category cat2 = new Category();
        cat2.setId(UUID.randomUUID());
        cat2.setSlug("coats");
        cat2.setNameEn("Coats");
        cat2.setDisplayOrder(2);

        when(categoryRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(List.of(cat1, cat2));

        List<CategoryDto> result = productService.listCategories();

        assertEquals(2, result.size());
        assertEquals("tops", result.get(0).getSlug());
        assertEquals("Tops", result.get(0).getName());
        assertEquals("coats", result.get(1).getSlug());
        assertNotNull(result.get(0).getId());
    }

    @Test
    void listCategories_emptyDatabase_returnsEmpty() {
        when(categoryRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(Collections.emptyList());

        List<CategoryDto> result = productService.listCategories();

        assertTrue(result.isEmpty());
    }

    // --- Upload Image Tests ---

    @Test
    void uploadImage_happyPath_returnsResponse() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProductId(productId)).thenReturn(0L);
        when(productImageRepository.saveAndFlush(any(ProductImage.class))).thenAnswer(invocation -> {
            ProductImage img = invocation.getArgument(0);
            ReflectionTestUtils.setField(img, "id", imageId);
            return img;
        });
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImageUploadResponse response = productService.uploadProductImage(productId, file, "image/jpeg", "Test alt");

        assertNotNull(response);
        assertNotNull(response.getImageId());
        assertNotNull(response.getImageUrl());
    }

    @Test
    void uploadImage_productNotFound_throwsResourceNotFoundException() throws Exception {
        UUID productId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.uploadProductImage(productId, file, "image/jpeg", "Test alt"));
    }

    @Test
    void uploadImage_unsupportedContentType_throwsValidationException() throws Exception {
        UUID productId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", new byte[]{1, 2, 3});

        assertThrows(ValidationException.class,
                () -> productService.uploadProductImage(productId, file, "image/gif", "Test alt"));
    }

    // --- Content Type Mapping Tests ---

    @Test
    void contentTypeToExtension_jpeg_returnsJpg() {
        assertEquals("jpg", productService.contentTypeToExtension("image/jpeg"));
    }

    @Test
    void contentTypeToExtension_png_returnsPng() {
        assertEquals("png", productService.contentTypeToExtension("image/png"));
    }

    @Test
    void contentTypeToExtension_webp_returnsWebp() {
        assertEquals("webp", productService.contentTypeToExtension("image/webp"));
    }

    @Test
    void contentTypeToExtension_unknown_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> productService.contentTypeToExtension("image/gif"));
    }

    // --- Helper Methods ---

    private Product createTestProduct(String name) {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        category.setSlug("tops");
        category.setNameEn("Tops");

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName(name);
        product.setDescription("Description of " + name);
        product.setShortDescription("Short: " + name);
        product.setCategory(category);
        product.setIsActive(true);
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedAt(OffsetDateTime.now());

        return product;
    }

    private ProductPrice createProductPrice(Product product, String currency, BigDecimal price) {
        ProductPrice pp = new ProductPrice();
        pp.setProduct(product);
        pp.setCurrency(currency);
        pp.setPrice(price);
        return pp;
    }
}
