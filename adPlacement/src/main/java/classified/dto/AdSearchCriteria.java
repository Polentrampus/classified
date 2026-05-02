package classified.dto;

import classified.entity.AdStatus;
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
}
