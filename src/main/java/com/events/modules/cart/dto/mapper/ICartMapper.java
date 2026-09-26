package com.events.modules.cart.dto.mapper;

import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.GetCartItemDto;
import com.events.modules.cart.entity.Cart;
import com.events.modules.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ICartMapper {

    GetCartDto toDto(Cart cart);

    GetCartItemDto toCartItemDto(CartItem cartItem);

    List<GetCartItemDto> toCartItemDtoList(List<CartItem> cartItems);
}
