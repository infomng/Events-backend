# GEMINI.md – Project Context

This file defines the global context of the project.
It must be used by Gemini (and any AI assistant) as the single source of truth
to understand the functional, technical, and architectural scope of the project.

---

## 1. Project Overview

### Project Name
Event Management Application

### Description
A web-based event management and ticketing platform.
The application allows organizers to create events, manage seats and tickets,
upload event images, and allow users to browse and reserve tickets.

### Project architecture
The project focuses on:
- Feature based architecture
- Clean code principles
- High test coverage
- Performance optimization
- Use of Supabase Storage for image handling
- Scalability
- Security
- Testability
- Maintainability

### Controllers 
The project uses the RestApi design pattern for controllers and adheres to the following principles:
- All controllers must return a Result Object.

### EXception handling
The project uses a global exception handling mechanism to ensure consistent error responses across all controllers.
When an exception occurs, it is caught by a centralized handler that maps exceptions to appropriate HTTP status codes and error messages.
When you need to throw exceptions in the code, BadRequestException or BusinessException should be used as much as possible.

---

## 2. Core Functionalities

### 2.1 Event Management
- Create, update, delete events
- Publish / unpublish events
- Event metadata:
    - Title
    - Description
    - Location
    - Date & time
    - Capacity
    - Event status (DRAFT, PUBLISHED, CANCELLED)
- Event categories (concert, conference, sport, etc.)

### 2.2 Image Management (Supabase Storage)
- Upload event images
- Store only file metadata in database (not the binary)
- Images stored in Supabase Storage buckets
- File naming:
    - UUID as filename
    - Original extension preserved
- Access strategy:
    - Public images for event listing
    - Optional signed URLs for private or admin access
- No sensitive data in URLs

### 2.3 Ticketing & Reservations
- Ticket types:
    - REGULAR
    - VIP
    - PREMIUM
- Ticket price per type
- Ticket availability per event
- Reservation lifecycle:
    - CREATED
    - CONFIRMED
    - CANCELLED
    - EXPIRED
- Prevent overbooking
- Seat locking mechanism (time-limited)

### 2.4 Seat Management
- Seat entities linked to events
- Seat properties:
    - Section (VIP, Balcony, etc.)
    - Row
    - Seat number
    - Seat type
- Seat availability tracking
- Seat reservation validation

### 2.5 Search & Filtering
- Dynamic search with optional filters:
    - Date range
    - Location
    - Category
    - Price range
    - Availability
- SQL dynamic filtering using:
    - Specifications (JPA Criteria API)
    - OR QueryDSL (future evolution)
- Pagination and sorting mandatory

---

## 3. Technical Stack

### Backend
- Java 21+
- Spring Boot
- Spring Web
- Spring Data JPA (Hibernate)
- PostgreSQL
- Redis (caching)
- Maven

### Security
- Token-based authentication
- Role-based access control (ADMIN / ORGANIZER / USER)
- Secure access to admin endpoints
- No business logic in controllers

### Storage
- Supabase Storage
- Buckets organized by domain (events, users, etc.)
- Metadata stored in database only

---

## 4. Architecture

### Architectural Style
- Clean Architecture
- Hexagonal / Ports & Adapters inspired
- Rich Domain Model

### Layering
- Controller layer (REST)
- Application / Service layer
- Domain layer
- Infrastructure layer

### Rules
- Controllers only orchestrate
- Business logic lives in services/domain
- No entity exposure directly to API
- DTOs for input/output
- Clear separation of concerns

---

## 5. Database Design Principles

- Tables in snake_case
- Primary keys:
    - BIGINT for business entities
    - UUID for public identifiers (optional)
- Foreign keys always explicit
- Indexes on:
    - event_id
    - date
    - location
    - status

---

## 6. Caching Strategy

- Redis used for:
    - Event listings
    - Event details
    - Frequently accessed reference data
- Cache invalidation on:
    - Event update
    - Ticket reservation
- No caching of sensitive data

---

## 7. Testing Strategy

### Unit Tests
- JUnit 5
- Mockito
- 100% coverage on:
    - Public service methods
    - Business rules

### Integration Tests
- Controllers tested with MockMvc
- Database interactions tested
- No mocked repositories for integration tests

### Code Coverage
- JaCoCo
- Coverage enforced in CI

---

## 8. Error Handling

- Global exception handling
- Meaningful error responses
- No stack traces exposed to clients
- Custom domain exceptions

---

## 9. Logging & Monitoring

- Structured logs
- Log levels:
    - INFO for business actions
    - WARN for recoverable issues
    - ERROR for failures
- No sensitive data in logs

---

## 10. API Design Guidelines

- RESTful endpoints
- Consistent naming
- HTTP status codes respected
- Pagination metadata in responses
- OpenAPI / Swagger documentation

---

## 11. Project Constraints

- No framework magic without explanation
- Prefer explicit code over shortcuts
- Long-term maintainability prioritized
- Scalability considered from design phase

---

## 12. How Gemini Should Assist

Gemini must:
- Respect the existing architecture
- Propose clean, production-ready code
- Always explain design choices
- Never put business logic in controllers
- Prefer readability over premature optimization
- Align suggestions with this context

Gemini must NOT:
- Introduce unnecessary frameworks
- Simplify business rules unrealistically
- Ignore existing decisions

---

## 13. Future Planned Features

- Payment integration (Stripe-like)
- QR code tickets
- Email notifications
- Admin dashboard
- Analytics (event attendance, sales)
- Multi-language support
- Rate limiting
- Audit logs

---

## 14. Decision Log

- Supabase chosen for storage simplicity
- UUID used for file naming
- Redis used as distributed cache
- Clean Architecture enforced from start
