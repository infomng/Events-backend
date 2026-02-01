package com.events.modules.event.repository;

import com.events.modules.event.entity.aggregate.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Check if a category exists by name (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if a category is associated with any events.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Event e WHERE e.category.id = :categoryId")
    boolean isCategoryUsedByEvents(@Param("categoryId") UUID categoryId);
}
