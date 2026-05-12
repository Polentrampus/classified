package com.classified.mappers;

import com.classified.dto.PromotionCreateRequest;
import com.classified.dto.PromotionResponse;
import com.classified.entity.Ad;
import com.classified.entity.Promotion;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class PromotionMapperImpl implements PromotionMapper {

    @Override
    public Promotion toEntity(PromotionCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Promotion promotion = new Promotion();

        promotion.setType( request.getType() );

        return promotion;
    }

    @Override
    public PromotionResponse toResponse(Promotion promotion) {
        if ( promotion == null ) {
            return null;
        }

        PromotionResponse.PromotionResponseBuilder promotionResponse = PromotionResponse.builder();

        promotionResponse.adId( promotionAdId( promotion ) );
        promotionResponse.id( promotion.getId() );
        promotionResponse.type( promotion.getType() );
        promotionResponse.startDate( promotion.getStartDate() );
        promotionResponse.endDate( promotion.getEndDate() );
        promotionResponse.createdAt( promotion.getCreatedAt() );

        return promotionResponse.build();
    }

    private Long promotionAdId(Promotion promotion) {
        if ( promotion == null ) {
            return null;
        }
        Ad ad = promotion.getAd();
        if ( ad == null ) {
            return null;
        }
        Long id = ad.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
