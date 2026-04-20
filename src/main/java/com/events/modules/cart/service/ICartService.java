package com.events.modules.cart.service;

import com.events.modules.cart.dto.AddToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.UpdateCartItemDto;

import java.util.UUID;

public interface ICartService {

    /**
     * Add an item to the current user's cart
     * @param addToCartDto the item to add
     */
    void addToCart(AddToCartDto addToCartDto);

    /**
     * Get the current user's active cart
     * @return the cart
     */
    GetCartDto getMyCart();

    /**
     * Remove an item from the cart
     * @param cartItemId the ID of the cart item to remove
     */
    void removeFromCart(UUID cartItemId);

    /**
     * Update the quantity of a cart item
     * @param updateDto the update data
     */
    void updateCartItem(UpdateCartItemDto updateDto);

    /**
     * Clear all items from the current user's cart
     */
    void clearCart();

    /**
     * Get cart total items count
     * @return number of items in cart
     */
    Integer getCartItemsCount();
}
