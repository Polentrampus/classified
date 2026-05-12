package com.classified.mappers;

import com.classified.dto.user.UserRegistrationRequest;
import com.classified.dto.user.UserResponse;
import com.classified.dto.user.UserUpdateRequest;
import com.classified.entity.Role;
import com.classified.entity.User;
import com.classified.entity.UserRating;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.name( request.getName() );
        user.lastName( request.getLastName() );
        user.email( request.getEmail() );
        user.phone( request.getPhone() );

        return user.build();
    }

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.role( userRoleName( user ) );
        BigDecimal rating = userUserRatingRating( user );
        if ( rating != null ) {
            userResponse.rating( rating );
        }
        else {
            userResponse.rating( new BigDecimal( "0.00" ) );
        }
        userResponse.id( user.getId() );
        userResponse.name( user.getName() );
        userResponse.lastName( user.getLastName() );
        userResponse.email( user.getEmail() );
        userResponse.phone( user.getPhone() );
        userResponse.createdAt( user.getCreatedAt() );

        return userResponse.build();
    }

    @Override
    public void updateEntityFromRequest(UserUpdateRequest request, User user) {
        if ( request == null ) {
            return;
        }

        user.setName( request.getName() );
        user.setLastName( request.getLastName() );
        user.setEmail( request.getEmail() );
        user.setPhone( request.getPhone() );
    }

    private String userRoleName(User user) {
        if ( user == null ) {
            return null;
        }
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        String name = role.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private BigDecimal userUserRatingRating(User user) {
        if ( user == null ) {
            return null;
        }
        UserRating userRating = user.getUserRating();
        if ( userRating == null ) {
            return null;
        }
        BigDecimal rating = userRating.getRating();
        if ( rating == null ) {
            return null;
        }
        return rating;
    }
}
