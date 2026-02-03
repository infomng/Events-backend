# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

### Build and compile
```bash
./mvnw clean install
./mvnw compile
```

### Run tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=EventServiceTest

# Run specific test method
./mvnw test -Dtest=EventServiceTest#createEvent_ShouldCreateSuccessfully

# Run tests with coverage (JaCoCo is configured but commented out)
./mvnw test jacoco:report
```

### Run the application
```bash
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Package the application
```bash
./mvnw package
```

## Architecture Overview

### Modular Feature-Based Structure

The codebase follows a **feature-based modular architecture** under `com.events.modules`:

- **auth**: Authentication, JWT management, OAuth2 integration, refresh tokens
- **user**: User management and profiles
- **event**: Core event management with price categories, seats, and reservations
- **category**: Event category management (refactored from event module)
- **country**: Country management for event locations
- **booking**: Booking lifecycle and reservation management
- **ticket**: Ticket generation and management
- **payment**: Payment processing
- **qrcode**: QR code generation for tickets

Each module contains its own:
- `dto/`: Data Transfer Objects (must be records ending with "Dto")
- `entity/`: JPA entities (may include `aggregate/` subdirectory for embedded entities)
- `repository/`: Spring Data JPA repositories (interfaces start with "I")
- `service/`: Business logic (must be `@Transactional` and return DTOs, not entities)
- `controller/`: REST endpoints (should return `Result<T>` wrapper)
- `exception/`: Module-specific exceptions
- `enumeration/`: Module-specific enums (must end with "Enum")
- `dto/mapper/`: MapStruct mappers (interfaces start with "I", in `..dto.mapper..` package)

### Common Infrastructure Layer

`com.events.common` provides shared infrastructure:

- **abstraction**: Base entities (`BaseEntity`, `AuditableEntity`)
- **result**: Result wrapper pattern (`Result<T>` for controller responses)
- **exception**: Global exception handling
- **config**: Spring Security, Hibernate filters, rate limiting, properties
- **supabase**: Supabase Storage integration for image uploads
- **utils**: Constants, pagination, string/file utilities

### Base Entity Hierarchy

All domain entities extend from:

1. **BaseEntity**: Provides UUID `id` and soft-delete `isActive` flag with Hibernate filters
2. **AuditableEntity**: Adds `createdAt`, `updatedAt`, `createdBy`, `updatedBy` via Spring Data JPA auditing

Example: `Event extends AuditableEntity extends BaseEntity`

Soft-delete implementation:
- Base approach: `isActive` flag on `BaseEntity` with `@Filter(name = "activeFilter")`
- Alternative approach: `@SQLDelete` and `@SQLRestriction` annotations (used by some entities like `PriceCategory`)
- Both approaches achieve soft-delete functionality

### Result Pattern

Controllers must wrap responses in `Result<T>`:
```java
Result.success(data)      // Success with data
Result.success()          // Success without data (returns EmptyResult)
Result.failure(error)     // Failure with ProblemDetail
```

Controllers return `ResponseEntity<Result<T>>` from endpoints

### Architecture Tests

ArchUnit tests enforce architecture rules in `src/test/java/com/events/architecture`:
- **ServiceArchitectureTest**: Services must be `@Transactional`
- **ServiceArchitectureTest**: Service public methods should not return JPA entities (except `UserService` and `AuthService`)
- **DtoArchitectureTest**: DTOs must be records with names ending in "Dto"
- **DtoArchitectureTest**: MapStruct mappers must reside in `..dto.mapper..` package
- **EnumNamingTest**: Enums must have names ending with "Enum"
- **InterfaceNamingTest**: Interfaces must start with "I"
- **RepositoryAccessTest**: Repository interfaces (e.g., `IUserRepository`) should only be accessed by their corresponding service (e.g., `UserService`)

## Key Design Patterns

### Entity Aggregates

The `Event` entity uses aggregate pattern with embedded value objects:
- `Event.images`: Set of `Image` (stored in Supabase, metadata in DB)
- `Event.seats`: Set of `Seat` with section, row, number
- `Event.priceCategories`: Set of `PriceCategory` with category name and price
- `Event.category`: Many-to-one relationship with `Category`
- `Event.country`: Many-to-one relationship with `Country`
- `Event.attendees`, `Event.staff`: Many-to-many with `User`

### Supabase Storage Integration

Images are stored in Supabase Storage, not the database:
- Files named with UUIDs
- Only metadata (URL, size, type) stored in `Image` entity
- Upload via `ISupabaseStorageService`
- Bucket name configured: `eventsimagebucket`

### MapStruct with Lombok

MapStruct mappers are configured with Lombok binding in `pom.xml`:
```xml
<path>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok-mapstruct-binding</artifactId>
    <version>0.2.0</version>
</path>
```

MapStruct version: 1.6.3

Mapper pattern:
```java
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {IOtherMapper.class})
public interface IEventMapper {
    EventDto toDto(Event event);
    Event toEntity(CreateEventCommandDto dto);
}
```

Mappers are Spring beans (via `componentModel = "spring"`) and can be injected into services

### OAuth2 + JWT Authentication

- OAuth2 (Google) handled by Spring Security
- Custom JWT tokens generated via `JwtService`
- Refresh tokens managed via `IRefreshTokenService`
- `JwtAuthenticationFilter` validates tokens
- Roles: `ADMIN`, `ORGANIZER`, `USER` (enum: `RoleEnum`)

## Exception Handling

Global exception handling via `GlobalExceptionHandler` with `@RestControllerAdvice`. Prefer throwing:
- `BadRequestException` for client errors (HTTP 400)
- `BusinessException` for business rule violations
- `ServerException` for server-side errors (HTTP 500)
- `ResourceNotFoundException` for missing resources (HTTP 404)
- Custom domain exceptions (e.g., `EventNotFoundException`, `EventForbidenException`, `CategoryNotFoundException`, `CategoryInUseException`, `CountryNotFoundException`, `CountryInUseException`)
- Auth exceptions (e.g., `UnauthorizedException`, `UserNotFoundException`, `InvalidRefreshTokenException`, `EmailAlreadyExistException`)

Exception handlers return `Result.failure(ProblemDetail)` with appropriate HTTP status codes.
Do not expose stack traces to clients.

## Database Configuration

- **DB**: PostgreSQL (localhost:5432/events)
- **H2**: Used for tests only
- **Hibernate DDL**: `update` mode (consider Flyway/Liquibase for production)
- **Dialect**: PostgreSQL
- **Active Filter**: Soft-delete via `@Filter(name = "activeFilter")` on `BaseEntity`

## Testing Guidelines

### Unit Tests
- Use Mockito for mocking dependencies
- Test service business logic with mocked repositories
- Example: `EventServiceTest` with `@ExtendWith(MockitoExtension.class)`

### Integration Tests
- Use `@SpringBootTest` for full context
- Use MockMvc for controller tests
- H2 in-memory database for integration tests

### Architecture Tests
- ArchUnit tests in `src/test/java/com/events/architecture`
- Run with `./mvnw test` to enforce architectural rules

## Important Notes

- **No business logic in controllers**: Controllers orchestrate only
- **DTOs for API boundaries**: Never expose JPA entities directly (except in `UserService`/`AuthService` where needed internally)
- **Always use MapStruct**: For entity-DTO conversions
- **Rate limiting**: Configured with Bucket4j (`bucket4j-core`)
- **OpenAPI docs**: Available at `/swagger-ui.html` (springdoc-openapi)
- **API versioning**: Base path is `/api/v1` (configured in `application.properties`)

## Related Documentation

- See `.gemini/GEMINI.md` for comprehensive project context (functional, technical, architectural)
- Project uses both Claude Code and Gemini - refer to GEMINI.md for AI assistant rules
- User stories tracked in `.gemini/user-stories/`
