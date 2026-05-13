package com.classified.dto.adType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdTypeCreateRequest {
    @NotNull(message = "ID типа продукта обязателен")
    private Long productTypeId;

    @NotNull(message = "ID категории обязателен")
    private Long categoryId;
}