package com.classified.mappers;

import com.classified.dto.order.OrderCreateRequest;
import com.classified.dto.order.OrderResponse;
import com.classified.dto.order.OrderUpdateRequest;
import com.classified.entity.Ad;
import com.classified.entity.Order;
import com.classified.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-12T13:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (JetBrains s.r.o.)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public Order toEntity(OrderCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        order.quantity( request.getQuantity() );
        order.totalPrice( request.getTotalPrice() );
        order.status( request.getStatus() );

        return order.build();
    }

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.adId( orderAdId( order ) );
        orderResponse.buyerId( orderBuyerId( order ) );
        orderResponse.sellerId( orderSellerId( order ) );
        orderResponse.createdAt( order.getCreatedAt() );
        orderResponse.updatedAt( order.getUpdatedAt() );
        orderResponse.completedAt( order.getCompletedAt() );
        orderResponse.id( order.getId() );
        orderResponse.quantity( order.getQuantity() );
        orderResponse.totalPrice( order.getTotalPrice() );
        orderResponse.status( order.getStatus() );

        return orderResponse.build();
    }

    @Override
    public void updateEntityFromRequest(OrderUpdateRequest request, Order order) {
        if ( request == null ) {
            return;
        }

        order.setQuantity( request.getQuantity() );
        order.setTotalPrice( request.getTotalPrice() );
        order.setStatus( request.getStatus() );
    }

    private Long orderAdId(Order order) {
        if ( order == null ) {
            return null;
        }
        Ad ad = order.getAd();
        if ( ad == null ) {
            return null;
        }
        Long id = ad.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long orderBuyerId(Order order) {
        if ( order == null ) {
            return null;
        }
        User buyer = order.getBuyer();
        if ( buyer == null ) {
            return null;
        }
        Long id = buyer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long orderSellerId(Order order) {
        if ( order == null ) {
            return null;
        }
        User seller = order.getSeller();
        if ( seller == null ) {
            return null;
        }
        Long id = seller.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
