package com.classified.dto.ad;

import com.classified.dto.AdStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdResponse {
    private Long id;
    private String title;
    private String description;
    private Long adTypeId;
    private BigDecimal price;
    private Integer quantity;
    private AdStatus adStatus;
    private Long sellerId;
    private Long addressId;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

}
