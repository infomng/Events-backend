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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private ICategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDto categoryDto;
    private CategoryCreateCommandDto createCommandDto;
    private CategoryUpdateCommandDto updateCommandDto;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = Category.builder()
                .name("Music")
                .description("Music events")
                .build();

        categoryDto = new CategoryDto(
                categoryId,
                "Music",
                "Music events"
        );

        createCommandDto = new CategoryCreateCommandDto("Music", "Music events");
        updateCommandDto = new CategoryUpdateCommandDto("Music Updated", "Updated description");
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_ShouldCreateSuccessfully() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase(createCommandDto.name())).thenReturn(false);
        when(categoryMapper.toEntity(createCommandDto)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        CategoryDto result = categoryService.createCategory(createCommandDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Music");
        verify(categoryRepository).existsByNameIgnoreCase(createCommandDto.name());
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toDto(category);
    }

    @Test
    @DisplayName("Should throw exception when creating category with duplicate name")
    void createCategory_ShouldThrowExceptionWhenNameExists() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase(createCommandDto.name())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createCommandDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository).existsByNameIgnoreCase(createCommandDto.name());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategory_ShouldUpdateSuccessfully() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase(updateCommandDto.name())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        CategoryDto result = categoryService.updateCategory(categoryId, updateCommandDto);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).updateEntityFromDto(updateCommandDto, category);
        verify(categoryRepository).save(category);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent category")
    void updateCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateCommandDto))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating category with duplicate name")
    void updateCategory_ShouldThrowExceptionWhenNameConflicts() throws Exception {
        // Given
        UUID otherCategoryId = UUID.randomUUID();
        Category otherCategory = Category.builder()
                .name("Sports")
                .build();

        // Use reflection to set the id field
        var idField = Category.class.getSuperclass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherCategory, otherCategoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase(updateCommandDto.name()))
                .thenReturn(Optional.of(otherCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateCommandDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete category successfully")
    void deleteCategory_ShouldDeleteSuccessfully() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.isCategoryUsedByEvents(categoryId)).thenReturn(false);

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).isCategoryUsedByEvents(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void deleteCategory_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting category in use")
    void deleteCategory_ShouldThrowExceptionWhenCategoryInUse() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.isCategoryUsedByEvents(categoryId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(CategoryInUseException.class)
                .hasMessageContaining("associated with one or more events");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).isCategoryUsedByEvents(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void getCategoryById_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        // When
        CategoryDto result = categoryService.getCategoryById(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Music");
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toDto(category);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent category")
    void getCategoryById_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("Should get all priceCategories successfully")
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = List.of(category);
        List<CategoryDto> categoryDtos = List.of(categoryDto);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDtoList(categories)).thenReturn(categoryDtos);

        // When
        List<CategoryDto> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Music");
        verify(categoryRepository).findAll();
        verify(categoryMapper).toDtoList(categories);
    }
}
