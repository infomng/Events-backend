package com.events.modules.category.controller;

import com.events.common.result.Result;
import com.events.modules.category.dto.CategoryCreateCommandDto;
import com.events.modules.category.dto.CategoryDto;
import com.events.modules.category.dto.CategoryUpdateCommandDto;
import com.events.modules.category.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for category management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Admin endpoints for managing event priceCategories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Create a new event category (ADMIN only)")
    public ResponseEntity<Result<CategoryDto>> createCategory(@Valid @RequestBody CategoryCreateCommandDto command) {
        CategoryDto category = categoryService.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a category", description = "Update an existing category (ADMIN only)")
    public ResponseEntity<Result<CategoryDto>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateCommandDto command) {
        CategoryDto category = categoryService.updateCategory(id, command);
        return ResponseEntity.ok(Result.success(category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Delete a category if not associated with events (ADMIN only)")
    public ResponseEntity<Result<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get category by ID", description = "Retrieve a category by its ID (ADMIN only)")
    public ResponseEntity<Result<CategoryDto>> getCategoryById(@PathVariable UUID id) {
        CategoryDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(Result.success(category));
    }

    @GetMapping
    @Operation(summary = "Get all priceCategories", description = "Retrieve all priceCategories (ADMIN only)")
    public ResponseEntity<Result<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(Result.success(categories));
    }
}
