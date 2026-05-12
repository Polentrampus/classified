package com.classified.mappers;

import com.classified.dto.ad.AdCreateRequest;
import com.classified.dto.ad.AdResponse;
import com.classified.dto.ad.AdUpdateRequest;
import com.classified.entity.Ad;
import com.classified.entity.AdType;
import com.classified.entity.Address;
import com.classified.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class AdMapperImpl implements AdMapper {

    @Override
    public Ad toEntity(AdCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Ad.AdBuilder ad = Ad.builder();

        ad.title( request.getTitle() );
        ad.description( request.getDescription() );
        ad.price( request.getPrice() );
        ad.quantity( request.getQuantity() );

        return ad.build();
    }

    @Override
    public AdResponse toResponse(Ad ad) {
        if ( ad == null ) {
            return null;
        }

        AdResponse.AdResponseBuilder adResponse = AdResponse.builder();

        adResponse.sellerId( adSellerId( ad ) );
        adResponse.addressId( adAddressId( ad ) );
        adResponse.adTypeId( adAdTypeId( ad ) );
        adResponse.id( ad.getId() );
        adResponse.title( ad.getTitle() );
        adResponse.description( ad.getDescription() );
        adResponse.price( ad.getPrice() );
        adResponse.quantity( ad.getQuantity() );
        adResponse.createdAt( ad.getCreatedAt() );

        return adResponse.build();
    }

    @Override
    public void updateEntityFromRequest(AdUpdateRequest request, Ad ad) {
        if ( request == null ) {
            return;
        }

        ad.setTitle( request.getTitle() );
        ad.setDescription( request.getDescription() );
        ad.setPrice( request.getPrice() );
        ad.setQuantity( request.getQuantity() );
    }

    private Long adSellerId(Ad ad) {
        if ( ad == null ) {
            return null;
        }
        User seller = ad.getSeller();
        if ( seller == null ) {
            return null;
        }
        Long id = seller.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long adAddressId(Ad ad) {
        if ( ad == null ) {
            return null;
        }
        Address address = ad.getAddress();
        if ( address == null ) {
            return null;
        }
        Long id = address.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long adAdTypeId(Ad ad) {
        if ( ad == null ) {
            return null;
        }
        AdType adType = ad.getAdType();
        if ( adType == null ) {
            return null;
        }
        Long id = adType.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
