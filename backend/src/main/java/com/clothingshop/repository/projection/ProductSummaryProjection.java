package com.clothingshop.repository.projection;

import java.time.Instant;
import java.util.UUID;

public interface ProductSummaryProjection {
    UUID getId();
    String getName();
    String getShortDescription();
    String getCategorySlug();
    String getCategoryName();
    String getImageObjectKey();
    Instant getCreatedAt();
}
