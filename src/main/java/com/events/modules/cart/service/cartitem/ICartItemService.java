package com.events.modules.cart.service.cartitem;

import com.events.modules.cart.dto.UpdateCartItemDto;

import java.util.UUID;

public interface ICartItemService {
    void deleteItem(UUID itemId);

    void updateCartItem(UpdateCartItemDto itemDto);
}
