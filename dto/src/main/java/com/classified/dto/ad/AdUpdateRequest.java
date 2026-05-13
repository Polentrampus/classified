package com.classified.dto.ad;

import com.classified.dto.AdStatus;
import com.classified.dto.image.AdImageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdUpdateRequest {
    private String title;
    private String description;
    private Long adTypeId;
    private BigDecimal price;
    private Integer quantity;
    private AdStatus adStatus;
    private Long addressId;
    private List<AdImageRequest> images;
}