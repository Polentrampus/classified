package com.classified.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdImageResponse {
    private Long id;
    private Long adId;
    private String url;
    private Boolean isMain;
    private LocalDateTime createdAt;
}
