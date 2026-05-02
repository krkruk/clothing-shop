package com.clothingshop.repository;

import com.clothingshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    long countByProductId(UUID productId);

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(UUID productId);

    Optional<ProductImage> findByProductIdAndId(UUID productId, UUID imageId);

    List<ProductImage> findByProductIdInOrderByDisplayOrderAsc(List<UUID> productIds);
}
