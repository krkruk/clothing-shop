package com.clothingshop.service;

import com.clothingshop.entity.Category;
import com.clothingshop.entity.ImageVariant;
import com.clothingshop.entity.Product;
import com.clothingshop.entity.ProductImage;
import com.clothingshop.entity.ProductPrice;
import com.clothingshop.exception.BadRequestException;
import com.clothingshop.exception.ResourceNotFoundException;
import com.clothingshop.exception.ValidationException;
import com.clothingshop.model.AdminProductListResponse;
import com.clothingshop.model.CategoryDto;
import com.clothingshop.model.CreateProductRequest;
import com.clothingshop.model.EthicsDto;
import com.clothingshop.model.FabricationDto;
import com.clothingshop.model.ImageUploadResponse;
import com.clothingshop.model.ProductDetailResponse;
import com.clothingshop.model.ProductImageDto;
import com.clothingshop.model.ProductListResponse;
import com.clothingshop.model.ProductPriceDto;
import com.clothingshop.model.ProductResponse;
import com.clothingshop.model.ProductSummary;
import com.clothingshop.model.UpdateProductRequest;
import com.clothingshop.repository.CategoryRepository;
import com.clothingshop.repository.ProductImageRepository;
import com.clothingshop.repository.ProductPriceRepository;
import com.clothingshop.repository.ProductRepository;
import com.clothingshop.repository.projection.ProductSummaryProjection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Base64;
import java.util.UUID;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("PLN", "EUR");
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductPriceRepository productPriceRepository;
    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository,
                          ProductPriceRepository productPriceRepository,
                          S3Client s3Client,
                          @Value("${minio.bucket}") String bucketName,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productPriceRepository = productPriceRepository;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.objectMapper = objectMapper;
    }

    public static String resolveCurrency(String currencyCode) {
        if (currencyCode != null && SUPPORTED_CURRENCIES.contains(currencyCode.toUpperCase())) {
            return currencyCode.toUpperCase();
        }
        return "PLN";
    }

    // --- createProduct ---
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        UUID categoryId = request.getCategoryId();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ValidationException("Category not found: " + categoryId));

        String sku = request.getSku();
        if (sku != null && productRepository.findBySku(sku).isPresent()) {
            throw new ValidationException("SKU already exists: " + sku);
        }

        // Validate prices
        validatePrices(request.getPrices());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setSku(sku);
        product.setCategory(category);
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getFabrication() != null) {
            product.setFabricationContent(request.getFabrication().getContent());
            product.setFabricationCare(request.getFabrication().getCare());
        }
        if (request.getEthics() != null) {
            product.setEthicsOrigin(request.getEthics().getOrigin());
            product.setEthicsImpact(request.getEthics().getImpact());
        }

        Product saved = productRepository.save(product);

        // Create price entities
        for (ProductPriceDto priceDto : request.getPrices()) {
            ProductPrice pp = new ProductPrice();
            pp.setProduct(saved);
            pp.setCurrency(priceDto.getCurrency());
            pp.setPrice(priceDto.getPrice());
            productPriceRepository.save(pp);
        }

        return toProductResponse(saved, category);
    }

    // --- updateProduct ---
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            product.setShortDescription(request.getShortDescription());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ValidationException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getSku() != null) {
            String newSku = request.getSku();
            if (product.getSku() == null || !product.getSku().equals(newSku)) {
                if (productRepository.findBySku(newSku).isPresent()) {
                    throw new ValidationException("SKU already exists: " + newSku);
                }
            }
            product.setSku(newSku);
        }

        if (request.getFabrication() != null) {
            product.setFabricationContent(request.getFabrication().getContent());
            product.setFabricationCare(request.getFabrication().getCare());
        }

        if (request.getEthics() != null) {
            product.setEthicsOrigin(request.getEthics().getOrigin());
            product.setEthicsImpact(request.getEthics().getImpact());
        }

        // Handle prices replacement (generated DTO defaults to empty list, treat empty as "not provided")
        if (request.getPrices() != null && !request.getPrices().isEmpty()) {
            validatePrices(request.getPrices());
            productPriceRepository.deleteAllByProductId(id);
            entityManager.flush();
            for (ProductPriceDto priceDto : request.getPrices()) {
                ProductPrice pp = new ProductPrice();
                pp.setProduct(product);
                pp.setCurrency(priceDto.getCurrency());
                pp.setPrice(priceDto.getPrice());
                productPriceRepository.save(pp);
            }
        }

        Product saved = productRepository.save(product);
        return toProductResponse(saved, saved.getCategory());
    }

    // --- deleteProduct (soft delete) ---
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    // --- listAdminProducts ---
    @Transactional(readOnly = true)
    public AdminProductListResponse listAdminProducts(String cursor, Integer limit) {
        int effectiveLimit = (limit != null) ? Math.min(Math.max(limit, 1), MAX_LIMIT) : DEFAULT_LIMIT;
        int pageSize = effectiveLimit + 1;

        Instant cursorTs = null;
        UUID cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            var decoded = decodeCursor(cursor);
            cursorTs = decoded.ts();
            cursorId = decoded.id();
        }

        List<Product> products = productRepository.findAllForAdmin(cursorTs, cursorId, pageSize);

        boolean hasMore = products.size() > effectiveLimit;
        List<Product> page = hasMore
                ? products.subList(0, effectiveLimit)
                : products;

        List<ProductResponse> items = page.stream()
                .map(this::toProductResponseFromEntity)
                .toList();

        String nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            Product last = page.get(page.size() - 1);
            nextCursor = encodeCursorFromProduct(last);
        }

        AdminProductListResponse response = new AdminProductListResponse();
        response.setItems(items);
        response.setNextCursor(nextCursor);
        response.setHasMore(hasMore);
        return response;
    }

    // --- deleteProductImage ---
    @Transactional
    public void deleteProductImage(UUID productId, UUID imageId) {
        ProductImage image = productImageRepository.findByProductIdAndId(productId, imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Image not found for product: productId=" + productId + ", imageId=" + imageId));

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(image.getObjectKey())
                    .build());
        } catch (Exception e) {
            // Log but continue - DB deletion is the source of truth
        }

        productImageRepository.delete(image);
    }

    // --- updateImageOrder ---
    @Transactional
    public void updateImageOrder(UUID productId, List<UUID> imageIds) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);

        Set<UUID> existingIds = new java.util.HashSet<>(images.stream().map(ProductImage::getId).toList());
        for (UUID imageId : imageIds) {
            if (!existingIds.contains(imageId)) {
                throw new ValidationException("Image " + imageId + " does not belong to product " + productId);
            }
        }

        for (int i = 0; i < imageIds.size(); i++) {
            final int order = i;
            UUID imageId = imageIds.get(i);
            images.stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .ifPresent(img -> img.setDisplayOrder(order));
        }

        productImageRepository.saveAll(images);
    }

    // --- listCategories ---
    @Transactional(readOnly = true)
    public List<CategoryDto> listCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc();
        return categories.stream()
                .map(cat -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setId(cat.getId());
                    dto.setSlug(cat.getSlug());
                    dto.setName(cat.getNameEn());
                    return dto;
                })
                .toList();
    }

    @Transactional
    public ImageUploadResponse uploadProductImage(UUID productId, MultipartFile file,
                                                   String contentType, String alt) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ValidationException("Unsupported content type: " + contentType +
                    ". Allowed: image/jpeg, image/png, image/webp");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String extension = contentTypeToExtension(contentType);
        String tempKey = "products/" + productId + "/tmp/original." + extension;

        long existingCount = productImageRepository.countByProductId(productId);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setObjectKey(tempKey);
        image.setVariant(ImageVariant.ORIGINAL.name());
        image.setAlt(alt);
        image.setDisplayOrder((int) existingCount);
        productImageRepository.saveAndFlush(image);

        UUID imageId = image.getId();
        String objectKey = "products/" + productId + "/" + imageId + "/original." + extension;
        image.setObjectKey(objectKey);
        productImageRepository.save(image);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to storage", e);
        }

        ImageUploadResponse response = new ImageUploadResponse();
        response.setImageId(imageId);
        response.setImageUrl(URI.create("/images/" + objectKey));
        return response;
    }

    // --- listProducts (public) ---
    @Transactional(readOnly = true)
    public ProductListResponse listProducts(String cursor, Integer limit, String categorySlug, String currency) {
        int effectiveLimit = (limit != null) ? Math.min(Math.max(limit, 1), MAX_LIMIT) : DEFAULT_LIMIT;
        int pageSize = effectiveLimit + 1;

        Instant cursorTs = null;
        UUID cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            var decoded = decodeCursor(cursor);
            cursorTs = decoded.ts();
            cursorId = decoded.id();
        }

        List<ProductSummaryProjection> projections = productRepository.findProductSummaries(
                categorySlug, cursorTs, cursorId, pageSize
        );

        boolean hasMore = projections.size() > effectiveLimit;
        List<ProductSummaryProjection> page = hasMore
                ? projections.subList(0, effectiveLimit)
                : projections;

        String resolvedCurrency = resolveCurrency(currency);

        List<ProductSummary> items = page.stream()
                .map(p -> toProductSummary(p, resolvedCurrency))
                .toList();

        String nextCursor = null;
        if (hasMore && !items.isEmpty()) {
            ProductSummaryProjection lastProjection = page.get(page.size() - 1);
            nextCursor = encodeCursor(lastProjection);
        }

        ProductListResponse response = new ProductListResponse();
        response.setItems(items);
        response.setNextCursor(nextCursor);
        response.setHasMore(hasMore);
        return response;
    }

    // --- getProductDetail (public) ---
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(UUID id, String currency) {
        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        Category category = product.getCategory();
        String resolvedCurrency = resolveCurrency(currency);

        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id);
        List<ProductPrice> prices = productPriceRepository.findAllByProductId(id);

        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setSku(product.getSku());

        // Set resolved price for requested currency
        prices.stream()
                .filter(pp -> pp.getCurrency().equals(resolvedCurrency))
                .findFirst()
                .ifPresent(pp -> {
                    response.setPrice(pp.getPrice());
                    response.setCurrency(pp.getCurrency());
                });

        // Set all prices array
        response.setPrices(prices.stream()
                .map(pp -> {
                    ProductPriceDto dto = new ProductPriceDto();
                    dto.setCurrency(pp.getCurrency());
                    dto.setPrice(pp.getPrice());
                    return dto;
                })
                .toList());

        if (!images.isEmpty()) {
            response.setImageUrl(URI.create("/images/" + images.get(0).getObjectKey()));
        }

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setSlug(category.getSlug());
        categoryDto.setName(category.getNameEn());
        response.setCategory(categoryDto);

        List<ProductImageDto> imageDtos = images.stream()
                .map(img -> {
                    ProductImageDto dto = new ProductImageDto();
                    dto.setImageId(img.getId());
                    dto.setImageUrl(URI.create("/images/" + img.getObjectKey()));
                    dto.setAlt(img.getAlt());
                    dto.setDisplayOrder(img.getDisplayOrder());
                    return dto;
                })
                .toList();
        response.setImages(imageDtos);

        if (product.getFabricationContent() != null || product.getFabricationCare() != null) {
            FabricationDto fab = new FabricationDto();
            fab.setContent(product.getFabricationContent());
            fab.setCare(product.getFabricationCare());
            response.setFabrication(fab);
        }

        if (product.getEthicsOrigin() != null || product.getEthicsImpact() != null) {
            EthicsDto ethics = new EthicsDto();
            ethics.setOrigin(product.getEthicsOrigin());
            ethics.setImpact(product.getEthicsImpact());
            response.setEthics(ethics);
        }

        return response;
    }

    // --- Validation ---

    private void validatePrices(List<ProductPriceDto> prices) {
        if (prices == null || prices.size() != 2) {
            throw new ValidationException("Prices must contain exactly 2 entries (PLN and EUR)");
        }

        boolean hasPLN = false;
        boolean hasEUR = false;

        for (ProductPriceDto priceDto : prices) {
            String currency = priceDto.getCurrency();
            if (!SUPPORTED_CURRENCIES.contains(currency)) {
                throw new ValidationException("Unsupported currency: " + currency + ". Supported: PLN, EUR");
            }
            if ("PLN".equals(currency)) hasPLN = true;
            if ("EUR".equals(currency)) hasEUR = true;

            BigDecimal priceValue = priceDto.getPrice();
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Price must be positive for currency " + currency);
            }
        }

        if (!hasPLN) {
            throw new ValidationException("PLN price is required");
        }
        if (!hasEUR) {
            throw new ValidationException("EUR price is required");
        }
    }

    // --- Cursor encoding/decoding ---

    private record CursorData(Instant ts, UUID id) {}

    private CursorData decodeCursor(String cursor) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(cursor));
            JsonNode node = objectMapper.readTree(json);
            Instant ts = Instant.parse(node.get("createdAt").asText());
            UUID id = UUID.fromString(node.get("id").asText());
            return new CursorData(ts, id);
        } catch (Exception e) {
            throw new BadRequestException("Invalid cursor value");
        }
    }

    private String encodeCursor(ProductSummaryProjection projection) {
        try {
            String json = objectMapper.writeValueAsString(
                    new CursorJson(projection.getCreatedAt().toString(), projection.getId().toString())
            );
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to encode cursor", e);
        }
    }

    private String encodeCursorFromProduct(Product product) {
        try {
            String json = objectMapper.writeValueAsString(
                    new CursorJson(product.getCreatedAt().toInstant().toString(), product.getId().toString())
            );
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to encode cursor", e);
        }
    }

    private record CursorJson(String createdAt, String id) {}

    // --- Helper methods ---

    String contentTypeToExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new ValidationException("Unsupported content type: " + contentType);
        };
    }

    private ProductSummary toProductSummary(ProductSummaryProjection p, String currency) {
        ProductSummary summary = new ProductSummary();
        summary.setId(p.getId());
        summary.setName(p.getName());
        summary.setShortDescription(p.getShortDescription());
        summary.setCurrency(currency);

        // Resolve price from product_price table based on currency
        productPriceRepository.findByProductIdAndCurrency(p.getId(), currency)
                .ifPresent(pp -> summary.setPrice(pp.getPrice()));

        if (p.getImageObjectKey() != null) {
            summary.setImageUrl(URI.create("/images/" + p.getImageObjectKey()));
        }

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setSlug(p.getCategorySlug());
        categoryDto.setName(p.getCategoryName());
        summary.setCategory(categoryDto);

        return summary;
    }

    private ProductResponse toProductResponse(Product product, Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setSlug(category.getSlug());
        categoryDto.setName(category.getNameEn());

        // Get prices
        List<ProductPrice> prices = productPriceRepository.findAllByProductId(product.getId());
        List<ProductPriceDto> priceDtos = prices.stream()
                .map(pp -> {
                    ProductPriceDto dto = new ProductPriceDto();
                    dto.setCurrency(pp.getCurrency());
                    dto.setPrice(pp.getPrice());
                    return dto;
                })
                .toList();

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setPrices(priceDtos);
        response.setSku(product.getSku());
        response.setIsActive(product.getIsActive());
        response.setCategory(categoryDto);
        response.setImages(new ArrayList<>());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getFabricationContent() != null || product.getFabricationCare() != null) {
            FabricationDto fab = new FabricationDto();
            fab.setContent(product.getFabricationContent());
            fab.setCare(product.getFabricationCare());
            response.setFabrication(fab);
        }

        if (product.getEthicsOrigin() != null || product.getEthicsImpact() != null) {
            EthicsDto ethics = new EthicsDto();
            ethics.setOrigin(product.getEthicsOrigin());
            ethics.setImpact(product.getEthicsImpact());
            response.setEthics(ethics);
        }

        return response;
    }

    private ProductResponse toProductResponseFromEntity(Product product) {
        Category category = product.getCategory();
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setSlug(category.getSlug());
        categoryDto.setName(category.getNameEn());

        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(product.getId());

        List<ProductImageDto> imageDtos = images.stream()
                .map(img -> {
                    ProductImageDto dto = new ProductImageDto();
                    dto.setImageId(img.getId());
                    dto.setImageUrl(URI.create("/images/" + img.getObjectKey()));
                    dto.setAlt(img.getAlt());
                    dto.setDisplayOrder(img.getDisplayOrder());
                    return dto;
                })
                .toList();

        // Get prices
        List<ProductPrice> prices = productPriceRepository.findAllByProductId(product.getId());
        List<ProductPriceDto> priceDtos = prices.stream()
                .map(pp -> {
                    ProductPriceDto dto = new ProductPriceDto();
                    dto.setCurrency(pp.getCurrency());
                    dto.setPrice(pp.getPrice());
                    return dto;
                })
                .toList();

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setPrices(priceDtos);
        response.setSku(product.getSku());
        response.setIsActive(product.getIsActive());
        response.setCategory(categoryDto);
        response.setImages(imageDtos);
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getFabricationContent() != null || product.getFabricationCare() != null) {
            FabricationDto fab = new FabricationDto();
            fab.setContent(product.getFabricationContent());
            fab.setCare(product.getFabricationCare());
            response.setFabrication(fab);
        }

        if (product.getEthicsOrigin() != null || product.getEthicsImpact() != null) {
            EthicsDto ethics = new EthicsDto();
            ethics.setOrigin(product.getEthicsOrigin());
            ethics.setImpact(product.getEthicsImpact());
            response.setEthics(ethics);
        }

        return response;
    }
}
