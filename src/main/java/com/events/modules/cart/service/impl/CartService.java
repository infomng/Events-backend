package com.events.modules.cart.service.impl;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.cart.dto.UpdateCartItemDto;
import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.mapper.ICartMapper;
import com.events.modules.cart.entity.Cart;
import com.events.modules.cart.entity.CartItem;
import com.events.modules.cart.exception.CartItemNotFoundException;
import com.events.modules.cart.exception.CartNotFoundException;
import com.events.modules.cart.exception.NotEnoughQuantityException;
import com.events.modules.cart.exception.TooMuchQuantityException;
import com.events.modules.cart.repository.ICartRepository;
import com.events.modules.cart.service.ICartService;
import com.events.modules.event.dto.GetEventDto;
import com.events.modules.event.dto.GetPriceCategoryDto;
import com.events.modules.event.exception.PriceCategoryNotFoundException;
import com.events.modules.event.service.IEventService;
import com.events.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
    private final IAuthService authService;
    private final ICartMapper cartMapper;

    @Override
    public void addItemToCart(AddItemToCartDto addItemToCartDto) {

        User currentUser = getCurrentUser();

        // Get or create active cart
        Cart cart = getCartOrCreateCarByUserId(currentUser.getId());

        // Validate event exists
        GetEventDto event = getEventById(addItemToCartDto.eventId());

        // Validate price category exists for this event
        GetPriceCategoryDto priceCategory = getPriceCategory(addItemToCartDto.priceCategoryId(), event);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = getExistingCartItem(addItemToCartDto, cart);

        int newQuantity = 0;
        if (existingItem.isPresent()) {
            newQuantity = computeNewQuantity(addItemToCartDto, existingItem.get());
            // Validate newQuantity
            validateNewQuantity(newQuantity, priceCategory.availableTickets());
        }

        if (existingItem.isPresent()) {
            // Update newQuantity
            cart.updateItem(existingItem.get(), newQuantity);
            cart.calculateTotalPrice();
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
                    .totalPrice(priceCategory.price().multiply(BigDecimal.valueOf(addItemToCartDto.quantity())))
                    .build();

            cart.addItem(newItem);
            cartRepository.save(cart);

            log.info("Added new item to cart for event: {}", event.name());
        }
    }

    public void removeCartItem(UUID cartItemId){
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId()).orElseThrow(
                () -> new CartNotFoundException(currentUser.getId()));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        cart.removeItem(cartItem);
    }

    private static int computeNewQuantity(AddItemToCartDto addItemToCartDto, CartItem existingItem) {
        return existingItem.getQuantity() + addItemToCartDto.quantity();
    }

    private static @NonNull Optional<CartItem> getExistingCartItem(AddItemToCartDto addItemToCartDto, Cart cart) {
        return cart.getItems()
                .stream()
                .filter(cartItem -> cartItem.getEventId().equals(addItemToCartDto.eventId())
                        && cartItem.getPriceCategoryId().equals(addItemToCartDto.priceCategoryId()))
                .findFirst();
    }

    private static @NonNull GetPriceCategoryDto getPriceCategory(UUID priceCategoryId, GetEventDto event) {
        return event.priceCategories().stream()
                .filter(pc -> pc.id().equals(priceCategoryId))
                .findFirst()
                .orElseThrow(() -> new PriceCategoryNotFoundException(event.id()));
    }

    private static void validateNewQuantity(int newQuantity, int availableTickets) {
        if (newQuantity < 0) {
            throw new NotEnoughQuantityException();
        }

        if (newQuantity > availableTickets) {
            throw new TooMuchQuantityException();
        }
    }

    private GetEventDto getEventById(UUID eventId) {
        return eventService.getEventById(eventId);
    }

    private @NonNull Cart getCartOrCreateCarByUserId(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private User getCurrentUser() {
        return authService.getCurrentUser();
    }

    @Override
    @Transactional
    public GetCartDto getCurrentUserCart() {
        User currentUser = getCurrentUser();

        Cart cart = getCartOrCreateCarByUserId(currentUser.getId());

        return cartMapper.toDto(cart);
    }


    @Override
    public void clearCart() {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new CartNotFoundException(currentUser.getId()));

        cart.clearItems();
        cartRepository.save(cart);

        log.info("Cleared cart for user {}", currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemsCount() {
        User currentUser = getCurrentUser();

        return cartRepository.findByUserId(currentUser.getId())
                .map(cart -> cart.getItems().size())
                .orElse(0);
    }

    private Cart createNewCart(UUID userId) {
        Cart newCart = Cart.builder()
                .userId(userId)
                .totalPrice(BigDecimal.ZERO)
                .build();

        return cartRepository.save(newCart);
    }
}
