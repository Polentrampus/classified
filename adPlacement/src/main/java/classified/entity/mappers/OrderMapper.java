package classified.entity.mappers;

import classified.dto.ad.AdUpdateRequest;
import classified.dto.order.OrderCreateRequest;
import classified.dto.order.OrderResponse;
import classified.dto.order.OrderUpdateRequest;
import classified.entity.Ad;
import classified.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    // OrderCreateRequest → Order
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Order toEntity(OrderCreateRequest request);

    // Order → OrderResponse
    @Mapping(target = "adId", source = "ad.id")
    @Mapping(target = "buyerId", source = "buyer.id")
    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "completedAt", source = "completedAt")
    OrderResponse toResponse(Order order);

    // OrderUpdateRequest → Order (in-place)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ad", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    void updateEntityFromRequest(OrderUpdateRequest request, @MappingTarget Order order);
}
