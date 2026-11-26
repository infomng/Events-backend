package com.events.modules.cart.service;

import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.GetCartItemDto;

import java.util.List;
import java.util.UUID;

public interface ICartService {

    void createCart();

    /**
     * Add an item to the current user's cart
     * @param addItemToCartDto the item to add
     */
    void addItemToCart(AddItemToCartDto addItemToCartDto);

    /**
     * Get the current user's active cart
     * @return the cart
     */
    GetCartDto getCurrentUserCart();

    /**
     * Remove an item from the cart
     * @param cartItemId the ID of the cart item to remove
     */
    void removeCartItem(UUID cartItemId);

    /**
     * Clear all items from the current user's cart
     */
    void clearCart();

    /**
     * Get cart total items count
     * @return number of items in cart
     */
    Integer getCartItemsCount();

    /**
     * Retire du panier de l'utilisateur les articles d'un événement et les retourne
     * (utilisé au moment de passer commande).
     * @param userId the cart owner
     * @param eventId the event being checked out
     * @return the removed items (never empty)
     */
    List<GetCartItemDto> takeItemsForEvent(UUID userId, UUID eventId);
}
