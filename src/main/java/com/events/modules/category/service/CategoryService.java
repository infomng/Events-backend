package com.events.modules.category.service;

import com.events.common.exception.BadRequestException;
import com.events.modules.category.dto.CategoryCreateCommandDto;
import com.events.modules.category.dto.CategoryDto;
import com.events.modules.category.dto.CategoryUpdateCommandDto;
import com.events.modules.category.dto.mapper.ICategoryMapper;
import com.events.modules.category.entity.Category;
import com.events.modules.category.exception.CategoryInUseException;
import com.events.modules.category.exception.CategoryNotFoundException;
import com.events.modules.category.repository.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService implements ICategoryService {

    private final ICategoryRepository categoryRepository;
    private final ICategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(CategoryCreateCommandDto command) {
        log.debug("Creating category with name: {}", command.name());

        // Check if category with the same name already exists
        if (categoryRepository.existsByNameIgnoreCase(command.name())) {
            throw new BadRequestException("Category with name '" + command.name() + "' already exists");
        }

        Category category = categoryMapper.toEntity(command);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with id: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(UUID id, CategoryUpdateCommandDto command) {
        log.debug("Updating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Check if new name conflicts with another category
        categoryRepository.findByNameIgnoreCase(command.name())
                .ifPresent(existingCategory -> {
                    if (!existingCategory.getId().equals(id)) {
                        throw new BadRequestException("Category with name '" + command.name() + "' already exists");
                    }
                });

        categoryMapper.updateEntityFromDto(command, category);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully with id: {}", id);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(UUID id) {
        log.debug("Deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        // Check if category is associated with any events
        if (categoryRepository.isCategoryUsedByEvents(id)) {
            throw new CategoryInUseException(id);
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CATEGORIES", key = "#id")
    public CategoryDto getCategoryById(UUID id) {
        log.debug("Fetching category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CATEGORIES")
    public List<CategoryDto> getAllCategories() {
        log.debug("Fetching all priceCategories");

        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }
}
