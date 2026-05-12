package com.classified.dto.adComment;

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
public class AdCommentResponse {
    private Long id;
    private Long orderId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}