package com.events.modules.cart.repository;

import com.events.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface ICartItemRepository extends JpaRepository<CartItem, UUID> {

//    @Query("""
//            SELECT ci
//            FROM CartItem ci
//            WHERE ci.cart.userId = :userId
//            AND ci.cart.id = :cartId
//            AND ci.eventId = :eventId
//            AND ci.priceCategoryId = :priceCategoryId
//            """)
//    Optional<CartItem> findByUserIdAndCartIdAndEventIdAndPriceCategoryId(
//            @Param("userId") UUID userId,
//            @Param("cartId") UUID cartId,
//            @Param("eventId") UUID eventId,
//            @Param("priceCategoryId") UUID priceCategoryId
//    );

    @Query("""
              SELECT ci FROM CartItem ci JOIN Cart ca WHERE ci.id =:itemId AND ca.userId =: userId
           """)
    Optional<CartItem> findByItemIdAndUserId(@Param("id") UUID itemId, @Param("userId") UUID userId);

//    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.id = :itemId")
//    Optional<CartItem> findByUserIdAndItemId(@Param("userId") UUID userId, @Param("itemId") UUID itemId);
}
