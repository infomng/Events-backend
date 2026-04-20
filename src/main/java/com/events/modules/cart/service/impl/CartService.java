package com.events.modules.cart.service.impl;

import com.events.modules.auth.service.auth.IAuthService;
import com.events.modules.cart.dto.AddToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.dto.UpdateCartItemDto;
import com.events.modules.cart.dto.mapper.ICartMapper;
import com.events.modules.cart.entity.Cart;
import com.events.modules.cart.entity.CartItem;
import com.events.modules.cart.enumeration.CartStatusEnum;
import com.events.modules.cart.exception.CartItemNotFoundException;
import com.events.modules.cart.exception.CartNotFoundException;
import com.events.modules.cart.exception.InvalidQuantityException;
import com.events.modules.cart.repository.ICartItemRepository;
import com.events.modules.cart.repository.ICartRepository;
import com.events.modules.cart.service.ICartService;
import com.events.modules.event.entity.Event;
import com.events.modules.event.entity.aggregate.PriceCategory;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.repository.IEventRepository;
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
    private final ICartItemRepository cartItemRepository;
    private final IEventRepository eventRepository;
    private final IAuthService authService;
    private final ICartMapper cartMapper;

    @Override
    public void addToCart(AddToCartDto addToCartDto) {
        // Validate quantity
        if (addToCartDto.quantity() == null || addToCartDto.quantity() < 1) {
            throw new InvalidQuantityException();
        }

        User currentUser = authService.getCurrentUser();

        // Get or create active cart
        Cart cart = cartRepository.findByUserIdAndStatus(currentUser.getId(), CartStatusEnum.ACTIVE)
                .orElseGet(() -> createNewCart(currentUser.getId()));

        // Validate event exists
        Event event = eventRepository.findById(addToCartDto.eventId())
                .orElseThrow(() -> new EventNotFoundException(addToCartDto.eventId()));

        // Validate price category exists for this event
        PriceCategory priceCategory = event.getPriceCategories().stream()
                .filter(pc -> pc.getId().equals(addToCartDto.priceCategoryId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Price category not found for this event"));

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndEventIdAndPriceCategoryId(
                cart.getId(),
                addToCartDto.eventId(),
                addToCartDto.priceCategoryId()
        );

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + addToCartDto.quantity());
            cartItemRepository.save(item);
            log.info("Updated cart item quantity for event: {}", event.getName());
        } else {
            // Create new cart item with snapshot data
            CartItem newItem = CartItem.builder()
                    .eventId(event.getId())
                    .eventName(event.getName())
                    .priceCategoryId(priceCategory.getId())
                    .priceCategoryName(priceCategory.getName())
                    .quantity(addToCartDto.quantity())
                    .unitPrice(priceCategory.getPrice())
                    .cart(cart)
                    .build();

            cart.addItem(newItem);
            cartItemRepository.save(newItem);
            log.info("Added new item to cart for event: {}", event.getName());
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCartDto getMyCart() {
        User currentUser = authService.getCurrentUser();

        Cart cart = cartRepository.findByUserIdAndStatus(currentUser.getId(), CartStatusEnum.ACTIVE)
                .orElseGet(() -> createNewCart(currentUser.getId()));

        return cartMapper.toDto(cart);
    }

    @Override
    public void removeFromCart(UUID cartItemId) {
        User currentUser = authService.getCurrentUser();

        CartItem cartItem = cartItemRepository.findByUserIdAndItemId(currentUser.getId(), cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        log.info("Removed item {} from cart", cartItemId);
    }

    @Override
    public void updateCartItem(UpdateCartItemDto updateDto) {
        if (updateDto.quantity() == null || updateDto.quantity() < 1) {
            throw new InvalidQuantityException();
        }

        User currentUser = authService.getCurrentUser();

        CartItem cartItem = cartItemRepository.findByUserIdAndItemId(currentUser.getId(), updateDto.cartItemId())
                .orElseThrow(() -> new CartItemNotFoundException(updateDto.cartItemId()));

        cartItem.setQuantity(updateDto.quantity());
        cartItemRepository.save(cartItem);

        // Recalculate cart total
        Cart cart = cartItem.getCart();
        cart.calculateTotal();
        cartRepository.save(cart);

        log.info("Updated cart item {} quantity to {}", updateDto.cartItemId(), updateDto.quantity());
    }

    @Override
    public void clearCart() {
        User currentUser = authService.getCurrentUser();

        Cart cart = cartRepository.findByUserIdAndStatus(currentUser.getId(), CartStatusEnum.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(currentUser.getId()));

        cart.clearItems();
        cartRepository.save(cart);

        log.info("Cleared cart for user {}", currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartItemsCount() {
        User currentUser = authService.getCurrentUser();

        return cartRepository.findByUserIdAndStatus(currentUser.getId(), CartStatusEnum.ACTIVE)
                .map(cart -> cart.getItems().size())
                .orElse(0);
    }

    private Cart createNewCart(UUID userId) {
        Cart newCart = Cart.builder()
                .userId(userId)
                .status(CartStatusEnum.ACTIVE)
                .total(BigDecimal.ZERO)
                .build();

        return cartRepository.save(newCart);
    }
}
