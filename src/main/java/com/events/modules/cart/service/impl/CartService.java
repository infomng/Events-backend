package com.events.modules.cart.service.impl;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.cart.dto.UpdateCartItemDto;
import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.mapper.ICartMapper;
import com.events.modules.cart.entity.Cart;
import com.events.modules.cart.entity.CartItem;
import com.events.modules.cart.exception.CartNotFoundException;
import com.events.modules.cart.exception.InvalidQuantityException;
import com.events.modules.cart.repository.ICartRepository;
import com.events.modules.cart.service.ICartService;
import com.events.modules.cart.service.cartitem.ICartItemService;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.dto.PriceCategoryDto;
import com.events.modules.event.exception.PriceCategoryNotFoundException;
import com.events.modules.event.service.IEventService;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService implements ICartService {

    private final ICartRepository cartRepository;
    private final IEventService eventService;
    private final ICartItemService cartItemService;
    private final IAuthService authService;
    private final ICartMapper cartMapper;

    @Override
    public void addItemToCart(AddItemToCartDto addItemToCartDto) {
        // Validate newQuantity
        if (addItemToCartDto.quantity() == null || addItemToCartDto.quantity() < 1) {
            throw new InvalidQuantityException();
        }

        User currentUser = authService.getCurrentUser();

        // Get or create active cart
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser.getId()));

        // Validate event exists
        GetEventDto event = eventService.getEventById(addItemToCartDto.eventId());

        // Validate price category exists for this event
        PriceCategoryDto priceCategory = event.priceCategories().stream()
                .filter(pc -> pc.id().equals(addItemToCartDto.priceCategoryId()))
                .findFirst()
                .orElseThrow(() -> new PriceCategoryNotFoundException(event.id()));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems()
                .stream()
                .filter(cartItem -> cartItem.getEventId().equals(addItemToCartDto.eventId())
                        && cartItem.getPriceCategoryId().equals(addItemToCartDto.priceCategoryId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update newQuantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + addItemToCartDto.quantity());
            cartRepository.save(cart);
            log.info("Updated cart item newQuantity for event: {}", event.name());
        } else {
            // Create new cart item with snapshot data
            CartItem newItem = CartItem.builder()
                    .eventId(event.id())
                    .eventName(event.name())
                    .priceCategoryId(priceCategory.id())
                    .priceCategoryName(priceCategory.name())
                    .quantity(addItemToCartDto.quantity())
                    .unitPrice(priceCategory.price())
                    .build();

            cart.addItem(newItem);

            cart.addItem(newItem);
            cartRepository.save(cart);

            log.info("Added new item to cart for event: {}", event.name());
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCartDto getMyCart() {
        User currentUser = authService.getCurrentUser();

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> createNewCart(currentUser.getId()));

        return cartMapper.toDto(cart);
    }

    @Override
    public void removeItemFromCart(UUID cartItemId) {
        cartItemService.deleteItem(cartItemId);
        log.info("Removed item {} from cart", cartItemId);
    }

    @Override
    public void updateCartItem(UpdateCartItemDto updateDto) {
        if (updateDto.newQuantity() == null || updateDto.newQuantity() < 1) {
            throw new InvalidQuantityException();
        }

        cartItemService.updateCartItem(updateDto);

//        cartItem.setQuantity(updateDto.newQuantity());
//        cartItemRepository.save(cartItem);
//
//        // Recalculate cart total
//        Cart cart = cartItem.getCart();
//        cart.calculateTotal();
//        cartRepository.save(cart);

        log.info("Updated cart item {} newQuantity to {}", updateDto.cartItemId(), updateDto.newQuantity());
    }

    @Override
    public void clearCart() {
        User currentUser = authService.getCurrentUser();

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new CartNotFoundException(currentUser.getId()));

        cart.clearItems();
        cartRepository.save(cart);

        log.info("Cleared cart for user {}", currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemsCount() {
        User currentUser = authService.getCurrentUser();

        return cartRepository.findByUserId(currentUser.getId())
                .map(cart -> cart.getItems().size())
                .orElse(0);
    }

    private Cart createNewCart(UUID userId) {
        Cart newCart = Cart.builder()
                .userId(userId)
                .total(BigDecimal.ZERO)
                .build();

        return cartRepository.save(newCart);
    }
}
