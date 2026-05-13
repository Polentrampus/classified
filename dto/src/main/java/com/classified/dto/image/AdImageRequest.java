package com.classified.dto.image;

import jakarta.validation.constraints.NotBlank;
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
public class AdImageRequest {

    @NotBlank(message = "URL изображения обязателен")
    private String url;

    private Boolean isMain;
}
