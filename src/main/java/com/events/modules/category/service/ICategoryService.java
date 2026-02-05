package com.events.modules.category.service;

import com.events.modules.category.dto.CategoryCreateCommandDto;
import com.events.modules.category.dto.CategoryDto;
import com.events.modules.category.dto.CategoryUpdateCommandDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for category management.
 * Only accessible by ADMIN users.
 */
public interface ICategoryService {

    /**
     * Create a new category.
     *
     * @param command the category creation data
     * @return the created category
     */
    CategoryDto createCategory(CategoryCreateCommandDto command);

    /**
     * Update an existing category.
     *
     * @param id      the category ID
     * @param command the category update data
     * @return the updated category
     */
    CategoryDto updateCategory(UUID id, CategoryUpdateCommandDto command);

    /**
     * Delete a category by ID.
     * Will fail if the category is associated with any events.
     *
     * @param id the category ID
     */
    void deleteCategory(UUID id);

    /**
     * Get a category by ID.
     *
     * @param id the category ID
     * @return the category
     */
    CategoryDto getCategoryById(UUID id);

    /**
     * Get all categories.
     *
     * @return list of all categories
     */
    List<CategoryDto> getAllCategories();
}
