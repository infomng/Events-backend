package com.events.modules.category.exception;

import com.events.common.exception.BadRequestException;

import java.util.UUID;

/**
 * Exception thrown when attempting to delete a category that is associated with events.
 */
public class CategoryInUseException extends BadRequestException {

    public CategoryInUseException(UUID categoryId) {
        super("Cannot delete category with id: " + categoryId + " because it is associated with one or more events");
    }

    public CategoryInUseException(String message) {
        super(message);
    }
}
