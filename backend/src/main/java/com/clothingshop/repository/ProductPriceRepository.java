package com.clothingshop.repository;

import com.clothingshop.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {

    List<ProductPrice> findAllByProductId(UUID productId);

    Optional<ProductPrice> findByProductIdAndCurrency(UUID productId, String currency);

    @Transactional
    void deleteAllByProductId(UUID productId);
}
