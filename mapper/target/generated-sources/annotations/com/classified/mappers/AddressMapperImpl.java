package com.classified.mappers;

import com.classified.dto.address.AddressCreateRequest;
import com.classified.dto.address.AddressResponse;
import com.classified.entity.Address;
import com.classified.entity.City;
import com.classified.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toEntity(AddressCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Address.AddressBuilder address = Address.builder();

        return address.build();
    }

    @Override
    public AddressResponse toResponse(Address address) {
        if ( address == null ) {
            return null;
        }

        AddressResponse.AddressResponseBuilder addressResponse = AddressResponse.builder();

        addressResponse.userId( addressUserId( address ) );
        addressResponse.cityId( addressCityId( address ) );
        addressResponse.id( address.getId() );

        return addressResponse.build();
    }

    private Long addressUserId(Address address) {
        if ( address == null ) {
            return null;
        }
        User user = address.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long addressCityId(Address address) {
        if ( address == null ) {
            return null;
        }
        City city = address.getCity();
        if ( city == null ) {
            return null;
        }
        Long id = city.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
