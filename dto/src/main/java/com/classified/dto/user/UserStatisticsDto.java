package com.classified.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserStatisticsDto {
    private Long userId;
    private String userName;
    private BigDecimal rating;
    private Long totalAds;
    private Long totalSales;
    private BigDecimal totalRevenue;
}