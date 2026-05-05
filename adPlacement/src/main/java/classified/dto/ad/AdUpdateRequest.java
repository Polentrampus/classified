package classified.dto.ad;

import classified.entity.AdStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
}