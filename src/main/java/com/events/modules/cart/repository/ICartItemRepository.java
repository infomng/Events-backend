package com.events.modules.cart.repository;

import com.events.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.eventId = :eventId AND ci.priceCategoryId = :priceCategoryId")
    Optional<CartItem> findByCartIdAndEventIdAndPriceCategoryId(
            @Param("cartId") UUID cartId,
            @Param("eventId") UUID eventId,
            @Param("priceCategoryId") UUID priceCategoryId
    );

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.id = :itemId")
    Optional<CartItem> findByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);
}
