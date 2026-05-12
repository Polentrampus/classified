package com.classified.mappers;

import com.classified.dto.adComment.AdCommentCreateRequest;
import com.classified.dto.adComment.AdCommentResponse;
import com.classified.entity.AdComment;
import com.classified.entity.Order;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class AdCommentMapperImpl implements AdCommentMapper {

    @Override
    public AdComment toEntity(AdCommentCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        AdComment.AdCommentBuilder adComment = AdComment.builder();

        adComment.rating( request.getRating() );
        adComment.content( request.getContent() );

        return adComment.build();
    }

    @Override
    public AdCommentResponse toResponse(AdComment comment) {
        if ( comment == null ) {
            return null;
        }

        AdCommentResponse.AdCommentResponseBuilder adCommentResponse = AdCommentResponse.builder();

        adCommentResponse.orderId( commentOrderId( comment ) );
        adCommentResponse.id( comment.getId() );
        adCommentResponse.rating( comment.getRating() );
        adCommentResponse.content( comment.getContent() );
        adCommentResponse.createdAt( comment.getCreatedAt() );

        return adCommentResponse.build();
    }

    private Long commentOrderId(AdComment adComment) {
        if ( adComment == null ) {
            return null;
        }
        Order order = adComment.getOrder();
        if ( order == null ) {
            return null;
        }
        Long id = order.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
