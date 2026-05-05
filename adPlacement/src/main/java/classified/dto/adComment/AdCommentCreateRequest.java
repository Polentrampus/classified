package classified.dto.adComment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class AdCommentCreateRequest {
    @NotNull
    private Long orderId;

    @NotNull @Min(1) @Max(5)
    private Integer rating;

    @NotBlank
    private String content;
}