# User Story Report: EVENTS-102

## Summary of Changes

This user story implements the functionality for an event organizer to publish an event. The following changes have been made:

- **EventController:**
    - A new endpoint `PATCH /api/v1/events/{id}/publish` has been added to handle the event publication.
    - The `publishEvent` method calls the `eventService` to perform the business logic.

- **IEventService:**
    - A new method `publishEvent(UUID eventId)` has been added to the interface.

- **EventService:**
    - The `publishEvent` method has been implemented with the following logic:
        - It retrieves the event by its ID and ensures that the current user is the organizer of the event.
        - It checks if the event is in the `DRAFT` status.
        - If the event is in the `DRAFT` status, it changes the status to `PUBLISHED`.
        - If the event is not in the `DRAFT` status, it throws a `BadRequestException`.

- **EventControllerTest:**
    - A new test method `testPublishEvent` has been added to verify the functionality of the `publishEvent` endpoint.

## Verification

All existing and new tests have been executed successfully, ensuring that the new functionality is working as expected and no regressions have been introduced.
