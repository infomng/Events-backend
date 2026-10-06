package com.events.modules.cart.testdatabuilder;

import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.entity.Cart;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.dto.GetPriceCategoryDto;
import com.events.modules.user.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartTestDataBuilder {
    private static final UUID PRICE_CATEGORY_ID = UUID.randomUUID();

    public static GetCartDto getCartDto() {
        return GetCartDto.builder()
                .userId(getUser().getId())
                .totalItems(2)
                .totalPrice(new java.math.BigDecimal("100.00"))
                .build();
    }

    public static Cart getCart() {
        return Cart.builder()
                .id(UUID.randomUUID())
                .userId(getUser().getId())
                .build();
    }

    public static GetEventDto getEventDto() {
        return GetEventDto.builder()
                .id(UUID.randomUUID())
                .name("Test Event")
                .priceCategories(List.of(getPriceCategoryDto()))
                .build();
    }

    public static GetPriceCategoryDto getPriceCategoryDto() {
        return GetPriceCategoryDto.builder()
                .id(PRICE_CATEGORY_ID)
                .name("VIP")
                .price(BigDecimal.valueOf(100.0))
                .totalTickets(10)
                .build();
    }

    public static User getUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@email.com")
                .fullName("John Doe")
                .build();
    }

    public static AddItemToCartDto getAddItemToCartDto() {
        return AddItemToCartDto.builder()
                .eventId(getEventDto().id())
                .priceCategoryId(getPriceCategoryDto().id())
                .quantity(1)
                .build();
    }
}
