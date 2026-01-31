# User Story Report: EVENTS-106

## User Story
EVENTS-106: Manage Event Categories

## Summary
Implemented the functionality for organizers to manage event categories (default and custom) with associated prices. This includes creating events with default or custom categories, and updating an event's categories.

## Changes Made

### 1. DTO and Entity Modifications
- **`PriceCategoryDto.java`**: Refactored from a class to a Java record to align with the project's architectural guidelines.
- **`Event.java`**: Added `orphanRemoval = true` to the `@OneToMany` relationship for the `eventCategories` collection. This ensures that categories removed from an event are properly deleted from the database.
- **`UpdateEventCommandDto.java`**: Added a `List<@Valid PriceCategoryDto> categories` field to allow event categories to be managed during the event update process.

### 2. Service Layer Implementation
- **`EventService.java`**:
    - Injected `IPriceCategoryMapper` to handle the mapping between `PriceCategoryDto` and the `PriceCategory` entity.
    - The private `updateEvent` method was made non-static and updated to include logic for replacing the event's categories with the list provided in the `UpdateEventCommandDto`.

### 3. Testing
- **`EventServiceTest.java`**:
    - Fixed an existing test that was broken by the refactoring of `PriceCategoryDto`.
    - Added a new nested test class `UpdateEventTests` with a test case to verify that event categories are correctly updated.
