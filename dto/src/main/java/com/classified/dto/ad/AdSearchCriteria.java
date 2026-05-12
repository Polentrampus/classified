package com.classified.dto.ad;

import com.classified.dto.AdStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdSearchCriteria {
    private String title;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private AdStatus status;
    private Long sellerId;
    private Long adTypeId;
    private Double minSellerRating;
    private Boolean promotedOnly;
}
