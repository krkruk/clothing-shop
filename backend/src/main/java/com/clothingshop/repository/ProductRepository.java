package com.clothingshop.repository;

import com.clothingshop.entity.Product;
import com.clothingshop.repository.projection.ProductSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = """
            SELECT p.id, p.name, p.short_description,
                   c.slug AS category_slug, c.name_en AS category_name,
                   pi.object_key AS image_object_key,
                   p.created_at
            FROM product p
            JOIN category c ON p.category_id = c.id
            LEFT JOIN LATERAL (
                SELECT pi.object_key FROM product_image pi
                WHERE pi.product_id = p.id
                ORDER BY pi.display_order ASC LIMIT 1
            ) pi ON true
            WHERE p.is_active = true
              AND (cast(:categorySlug as text) IS NULL OR c.slug = cast(:categorySlug as text))
              AND (cast(:cursorTs as timestamptz) IS NULL
                   OR (p.created_at > cast(:cursorTs as timestamptz))
                   OR (p.created_at = cast(:cursorTs as timestamptz) AND p.id > cast(:cursorId as uuid)))
            ORDER BY p.created_at ASC, p.id ASC
            LIMIT :pageSize
            """, nativeQuery = true)
    List<ProductSummaryProjection> findProductSummaries(
            @Param("categorySlug") String categorySlug,
            @Param("cursorTs") Instant cursorTs,
            @Param("cursorId") UUID cursorId,
            @Param("pageSize") int pageSize
    );

    Optional<Product> findByIdAndIsActiveTrue(UUID id);

    @Query(value = """
            SELECT p.* FROM product p
            WHERE (cast(:cursorTs as timestamptz) IS NULL
                   OR (p.created_at < cast(:cursorTs as timestamptz))
                   OR (p.created_at = cast(:cursorTs as timestamptz) AND p.id < cast(:cursorId as uuid)))
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :pageSize
            """, nativeQuery = true)
    List<Product> findAllForAdmin(
            @Param("cursorTs") Instant cursorTs,
            @Param("cursorId") UUID cursorId,
            @Param("pageSize") int pageSize
    );

    Optional<Product> findBySku(String sku);
}
