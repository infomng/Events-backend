package com.events.modules.cart.controller;

import com.events.common.result.Result;
import com.events.modules.cart.dto.AddItemToCartDto;
import com.events.modules.cart.dto.GetCartDto;
import com.events.modules.cart.service.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final ICartService cartService;

    @PostMapping
    public ResponseEntity<Result<Void>> createCart() {
        cartService.createCart();
        return ResponseEntity.ok(Result.success());
    }

    @PutMapping("/items")
    public ResponseEntity<Result<Void>> addItemToCart(@Valid @RequestBody AddItemToCartDto addItemToCartDto) {
        cartService.addItemToCart(addItemToCartDto);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping
    public ResponseEntity<Result<GetCartDto>> getCurrentUserCart() {
        GetCartDto cart = cartService.getCurrentUserCart();
        return ResponseEntity.ok(Result.success(cart));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Result<Void>> removeFromCart(@PathVariable UUID cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(Result.success());
    }

    @DeleteMapping
    public ResponseEntity<Result<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/count")
    public ResponseEntity<Result<Integer>> getCartItemsCount() {
        Integer count = cartService.getCartItemsCount();
        return ResponseEntity.ok(Result.success(count));
    }
}
