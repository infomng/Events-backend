# Payment Module Documentation

## Overview

The Payment module handles the complete payment lifecycle for event ticket bookings in the Events application. It supports multiple payment gateways (Stripe, PayPal, mobile money providers), multiple payment methods, and comprehensive payment state management.

**Current Status**: Core implementation complete, pending gateway integrations and downstream integrations.

---

## What Has Been Implemented

### 1. Core Entity - Payment.java

**Location**: `entity/Payment.java`

The Payment entity extends `AuditableEntity` (which provides soft-delete and auditing capabilities) and includes:

**Fields**:
- `userId` - User who made the payment
- `bookingId` - Associated booking (one-to-one relationship)
- `amount` - Payment amount (BigDecimal, 19,2 precision)
- `currency` - Currency code (default: USD)
- `status` - Current payment status (PaymentStatusEnum)
- `paymentMethod` - Method used (credit card, PayPal, mobile money, etc.)
- `paymentGateway` - Gateway provider (Stripe, PayPal, Orange Money, etc.)
- `transactionId` - External transaction ID from payment gateway (unique)
- `paymentReference` - Internal reference in format PAY-XXXXX (unique)
- `paidAt` - Timestamp when payment was completed
- `gatewayResponse` - JSON response from payment gateway
- `failureReason` - Reason if payment failed
- `metadata` - Additional JSON metadata

**Helper Methods**:
- `markAsPaid(String transactionId)` - Marks payment as COMPLETED
- `markAsFailed(String reason)` - Marks payment as FAILED
- `markAsRefunded()` - Marks payment as REFUNDED
- `generatePaymentReference()` - Auto-generates reference on persist (PAY-XXXXX format)

**Inherited from AuditableEntity**:
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy` - Automatic auditing
- `isActive` - Soft-delete flag

### 2. Enumerations

#### PaymentStatusEnum
```java
PENDING      // Payment initiated, awaiting processing
PROCESSING   // Being processed by gateway
COMPLETED    // Successfully completed
FAILED       // Payment failed
CANCELLED    // Cancelled by user
REFUNDED     // Refunded to user
```

#### PaymentMethodEnum
```java
CREDIT_CARD, DEBIT_CARD, PAYPAL, STRIPE,
MOBILE_MONEY, BANK_TRANSFER, CASH
```

#### PaymentGatewayEnum
```java
STRIPE, PAYPAL, ORANGE_MONEY, MTN_MONEY,
WAVE, FLUTTERWAVE, INTERNAL
```

### 3. Data Transfer Objects (DTOs)

All DTOs are records following the project architecture:

- **InitiatePaymentDto** - Request to start a payment (bookingId, amount, currency, method, gateway, returnUrl, metadata)
- **ConfirmPaymentDto** - Confirm payment completion (paymentReference, transactionId, gatewayResponse)
- **RefundPaymentDto** - Request payment refund (paymentId, reason)
- **GetPaymentDto** - Complete payment details for response
- **PaymentResponseDto** - Response after initiation (paymentId, reference, status, redirectUrl, message)

**MapStruct Mapper**: `IPaymentMapper` in `dto.mapper` package
- `toDto(Payment)` - Entity to DTO
- `toDtoList(List<Payment>)` - List conversion
- `toEntity(InitiatePaymentDto)` - DTO to entity

### 4. Repository - IPaymentRepository

**Location**: `repository/IPaymentRepository.java`

Custom query methods implemented:
```java
Optional<Payment> findByPaymentReference(String paymentReference)
Optional<Payment> findByTransactionId(String transactionId)
Optional<Payment> findByBookingId(UUID bookingId)
Page<Payment> findByUserId(UUID userId, Pageable pageable)
List<Payment> findByUserIdAndStatus(UUID userId, PaymentStatusEnum status)
Optional<Payment> findByUserIdAndPaymentId(UUID userId, UUID paymentId)
boolean existsByBookingId(UUID bookingId)
```

### 5. Service Layer - PaymentService

**Location**: `service/impl/PaymentService.java`

**Implemented Methods**:

1. **initiatePayment(InitiatePaymentDto)**: PaymentResponseDto
   - Validates no existing payment for booking
   - Creates payment with PENDING status
   - Generates unique payment reference
   - Returns redirect URL for gateway (currently mock)
   - **TODO**: Integrate with real payment gateway API

2. **confirmPayment(ConfirmPaymentDto)**: void
   - Validates payment exists and is PENDING
   - Marks as COMPLETED with transaction ID
   - Stores gateway response
   - **TODO**: Update booking status to CONFIRMED
   - **TODO**: Trigger ticket generation
   - **TODO**: Send confirmation email

3. **getPaymentById(UUID)**: GetPaymentDto
   - Retrieves payment by ID
   - Validates user ownership

4. **getPaymentByReference(String)**: GetPaymentDto
   - Retrieves payment by reference
   - Validates user ownership

5. **getMyPayments(int page, int size)**: Page<GetPaymentDto>
   - Paginated list of user's payments
   - Sorted by creation date (newest first)

6. **getMyPaymentsByStatus(PaymentStatusEnum)**: List<GetPaymentDto>
   - Filters user payments by status

7. **cancelPayment(UUID)**: void
   - Validates payment is PENDING or PROCESSING
   - Marks as CANCELLED
   - **TODO**: Release booking reservation if applicable

8. **refundPayment(RefundPaymentDto)**: void
   - Validates payment is COMPLETED
   - Marks as REFUNDED
   - Stores refund reason
   - **TODO**: Process actual refund via gateway API
   - **TODO**: Update booking status
   - **TODO**: Cancel/invalidate tickets
   - **TODO**: Send refund confirmation email

9. **checkPaymentStatus(String)**: PaymentStatusEnum
   - Returns current payment status by reference
   - No authentication required (public endpoint)

**Security Features**:
- All operations validate user ownership
- Status transition validation prevents invalid state changes
- Duplicate payment prevention via `existsByBookingId`
- Current user retrieved via `IAuthService`

### 6. Controller - PaymentController

**Location**: `controller/PaymentController.java`

**API Endpoints** (Base: `/api/v1/payments`):

```
POST   /initiate                        - Initiate payment
POST   /confirm                         - Confirm payment (webhook)
GET    /{paymentId}                     - Get payment by ID
GET    /reference/{paymentReference}    - Get payment by reference
GET    /my-payments                     - Get all user payments (paginated)
GET    /my-payments/status/{status}     - Filter payments by status
DELETE /{paymentId}/cancel              - Cancel pending payment
POST   /refund                          - Refund completed payment
GET    /status/{paymentReference}       - Check payment status (public)
```

All endpoints return `ResponseEntity<Result<T>>` following the Result pattern.
All endpoints (except status check) require JWT authentication.

### 7. Custom Exceptions

Four domain-specific exceptions in `exception/` package:

- **PaymentNotFoundException** - Payment not found
- **PaymentFailedException** - Payment processing failed
- **InvalidPaymentStatusException** - Invalid status transition
- **PaymentAlreadyProcessedException** - Payment already processed

All handled by `GlobalExceptionHandler` with appropriate HTTP status codes.

### 8. Internationalization (i18n)

The service uses `LocalizationService` for error messages:
- `payment.not.found.id`
- `payment.not.found.reference`
- `payment.already.exists`
- `payment.already.processed.reference`
- `payment.invalid.status`
- `payment.invalid.status.cancel`
- `payment.invalid.status.refund`
- `payment.unauthorized`
- `payment.redirect.message`

**Location**: `src/main/resources/i18n/` (message bundles)

### 9. API Testing Files

11 Bruno test files created in `http/Payment/`:
- InitiatePayment.bru
- ConfirmPayment.bru
- GetPaymentById.bru
- GetPaymentByReference.bru
- GetMyPayments.bru
- GetPaymentsByStatus.bru
- CancelPayment.bru
- RefundPayment.bru
- CheckPaymentStatus.bru

All configured with Bearer token authentication and environment variables.

---

## What You Need To Do To Complete The Module

### CRITICAL - Payment Gateway Integration

**Priority**: HIGH

**Action Required**:

1. **Add Payment Gateway Dependencies**

   Add to `pom.xml`:
   ```xml
   <!-- Stripe -->
   <dependency>
       <groupId>com.stripe</groupId>
       <artifactId>stripe-java</artifactId>
       <version>23.0.0</version>
   </dependency>

   <!-- PayPal (if needed) -->
   <dependency>
       <groupId>com.paypal.sdk</groupId>
       <artifactId>rest-api-sdk</artifactId>
       <version>1.14.0</version>
   </dependency>
   ```

2. **Create Gateway Service Interfaces**

   Create in `service/gateway/` package:
   ```
   IPaymentGatewayService.java - Generic interface
   StripePaymentGatewayService.java - Stripe implementation
   PayPalPaymentGatewayService.java - PayPal implementation
   MobileMoneyGatewayService.java - Mobile money providers
   ```

3. **Implement Stripe Integration**

   In `StripePaymentGatewayService`:
   - Initialize Stripe client with API key
   - Implement `createPaymentIntent()` method
   - Implement `confirmPayment()` method
   - Implement `refundPayment()` method
   - Handle Stripe exceptions and map to domain exceptions

4. **Add Gateway Configuration**

   In `application.properties`:
   ```properties
   # Stripe
   stripe.api.key=${STRIPE_SECRET_KEY}
   stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET}

   # PayPal
   paypal.client.id=${PAYPAL_CLIENT_ID}
   paypal.client.secret=${PAYPAL_CLIENT_SECRET}
   paypal.mode=sandbox # or live
   ```

5. **Update PaymentService.initiatePayment()**

   Replace mock redirect URL generation with:
   ```java
   // Inject appropriate gateway service based on dto.paymentGateway()
   IPaymentGatewayService gatewayService = getGatewayService(dto.paymentGateway());

   // Create payment intent with gateway
   GatewayPaymentResponse response = gatewayService.createPaymentIntent(
       dto.amount(),
       dto.currency(),
       payment.getPaymentReference()
   );

   // Store gateway-specific data
   payment.setMetadata(response.getMetadata());

   // Return real redirect URL
   return new PaymentResponseDto(
       payment.getId(),
       payment.getPaymentReference(),
       payment.getStatus(),
       response.getRedirectUrl(),
       localizationService.getMessage("payment.redirect.message")
   );
   ```

6. **Create Webhook Handlers**

   Create new controller: `PaymentWebhookController.java`
   ```java
   @PostMapping("/api/v1/webhooks/stripe")
   public ResponseEntity<Void> handleStripeWebhook(
       @RequestBody String payload,
       @RequestHeader("Stripe-Signature") String signature
   ) {
       // Verify webhook signature
       // Parse event
       // Call confirmPayment() or handle failure
       return ResponseEntity.ok().build();
   }
   ```

### CRITICAL - Booking Integration

**Priority**: HIGH

**Action Required**:

1. **Add Booking Service Dependency**

   In `PaymentService.java`:
   ```java
   private final IBookingService bookingService;
   ```

2. **Update confirmPayment() Method**

   After marking payment as paid:
   ```java
   payment.markAsPaid(dto.transactionId());
   paymentRepository.save(payment);

   // NEW: Update booking status
   bookingService.confirmBooking(payment.getBookingId());

   log.info("Payment confirmed and booking updated: {}", payment.getBookingReference());
   ```

3. **Update refundPayment() Method**

   After marking as refunded:
   ```java
   payment.markAsRefunded();
   paymentRepository.save(payment);

   // NEW: Cancel booking
   bookingService.cancelBooking(payment.getBookingId(), dto.reason());

   log.info("Payment refunded and booking cancelled: {}", payment.getPaymentReference());
   ```

4. **Add Payment Amount Validation**

   In `initiatePayment()`:
   ```java
   // NEW: Validate payment amount matches booking total
   Booking booking = bookingService.getBookingById(dto.bookingId());
   if (dto.amount().compareTo(booking.getTotalAmount()) != 0) {
       throw new InvalidPaymentAmountException(
           localizationService.getMessage("payment.amount.mismatch")
       );
   }
   ```

### CRITICAL - Ticket Generation

**Priority**: HIGH

**Action Required**:

1. **Add Ticket Service Dependency**

   In `PaymentService.java`:
   ```java
   private final ITicketService ticketService;
   ```

2. **Update confirmPayment() Method**

   After booking confirmation:
   ```java
   bookingService.confirmBooking(payment.getBookingId());

   // NEW: Generate tickets
   ticketService.generateTicketsForBooking(payment.getBookingId());

   log.info("Tickets generated for booking: {}", payment.getBookingId());
   ```

3. **Update refundPayment() Method**

   After booking cancellation:
   ```java
   bookingService.cancelBooking(payment.getBookingId(), dto.reason());

   // NEW: Invalidate tickets
   ticketService.invalidateTicketsForBooking(payment.getBookingId());

   log.info("Tickets invalidated for booking: {}", payment.getBookingId());
   ```

### HIGH PRIORITY - Email Notifications

**Priority**: HIGH

**Action Required**:

1. **Create Email Service (if not exists)**

   Create `com.events.common.email/`:
   ```
   IEmailService.java
   EmailService.java
   EmailTemplateLoader.java
   ```

2. **Add Email Templates**

   Create in `src/main/resources/templates/email/`:
   ```
   payment-confirmation.html
   payment-failure.html
   payment-refund.html
   ```

3. **Add Email Dependency**

   In `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-mail</artifactId>
   </dependency>
   ```

4. **Configure Email**

   In `application.properties`:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=${EMAIL_USERNAME}
   spring.mail.password=${EMAIL_PASSWORD}
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

5. **Update Payment Service**

   Inject and use email service:
   ```java
   private final IEmailService emailService;

   // In confirmPayment()
   emailService.sendPaymentConfirmationEmail(
       currentUser.getEmail(),
       payment.getPaymentReference(),
       payment.getAmount()
   );

   // In refundPayment()
   emailService.sendRefundConfirmationEmail(
       currentUser.getEmail(),
       payment.getPaymentReference(),
       payment.getAmount()
   );
   ```

### MEDIUM PRIORITY - Admin Endpoints

**Priority**: MEDIUM

**Action Required**:

1. **Add Admin Payment Endpoints**

   In `PaymentController.java` or new `AdminPaymentController.java`:
   ```java
   @GetMapping("/admin/payments")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<Result<Page<GetPaymentDto>>> getAllPayments(
       @RequestParam(defaultValue = "0") int page,
       @RequestParam(defaultValue = "20") int size
   ) {
       // Implementation
   }

   @GetMapping("/admin/payments/statistics")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<Result<PaymentStatisticsDto>> getPaymentStatistics() {
       // Return total revenue, payment counts by status, etc.
   }

   @PostMapping("/admin/payments/{paymentId}/manual-refund")
   @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<Result<Void>> manualRefund(
       @PathVariable UUID paymentId,
       @RequestBody AdminRefundDto dto
   ) {
       // Admin-initiated refund
   }
   ```

### MEDIUM PRIORITY - Payment Security Enhancements

**Priority**: MEDIUM

**Action Required**:

1. **Add Rate Limiting**

   Add `@RateLimit` annotation to payment endpoints:
   ```java
   @PostMapping("/initiate")
   @RateLimit(maxRequests = 5, duration = "1m")
   public ResponseEntity<Result<PaymentResponseDto>> initiatePayment(...) {
       // Implementation
   }
   ```

2. **Add Webhook Signature Verification**

   In `PaymentWebhookController`:
   ```java
   private boolean verifyStripeSignature(String payload, String signature) {
       try {
           Event event = Webhook.constructEvent(
               payload,
               signature,
               webhookSecret
           );
           return true;
       } catch (SignatureVerificationException e) {
           log.error("Invalid webhook signature", e);
           return false;
       }
   }
   ```

3. **Add Payment Timeout Handling**

   Create scheduled task to mark old PENDING payments as EXPIRED:
   ```java
   @Scheduled(fixedRate = 300000) // Every 5 minutes
   public void expireOldPayments() {
       LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
       List<Payment> expiredPayments = paymentRepository
           .findByStatusAndCreatedAtBefore(PaymentStatusEnum.PENDING, cutoff);

       expiredPayments.forEach(payment -> {
           payment.setStatus(PaymentStatusEnum.CANCELLED);
           payment.setFailureReason("Payment expired");
       });

       paymentRepository.saveAll(expiredPayments);
   }
   ```

### MEDIUM PRIORITY - Testing

**Priority**: MEDIUM

**Action Required**:

1. **Create Unit Tests**

   Create `src/test/java/com/events/modules/payment/`:
   ```
   PaymentServiceTest.java - Mock service layer tests
   PaymentMapperTest.java - MapStruct mapping tests
   PaymentRepositoryTest.java - Repository query tests
   ```

2. **Create Integration Tests**

   Create `PaymentIntegrationTest.java`:
   - Test full payment workflow
   - Test with MockMvc for controller endpoints
   - Test with H2 in-memory database

3. **Test Gateway Integration**

   Create `StripeGatewayServiceTest.java`:
   - Use Stripe test API keys
   - Test payment intent creation
   - Test webhook handling
   - Test refund processing

### LOW PRIORITY - Advanced Features

**Priority**: LOW

**Future Enhancements**:

1. **Partial Refunds**: Support refunding only part of payment amount
2. **Payment Splitting**: Allow multiple payment methods for one booking
3. **Recurring Payments**: Support for subscription-based events
4. **Payment Plans**: Installment payment support
5. **Invoice Generation**: PDF invoice creation and download
6. **Multi-Currency**: Automatic currency conversion
7. **Payment Analytics Dashboard**: Charts and reports for payment data

---

## Integration Summary

### Current Integrations
- ✅ **Auth Module**: Retrieves authenticated user via `IAuthService`
- ✅ **Security**: JWT authentication on all endpoints (except status check)
- ✅ **i18n**: Localized error messages via `LocalizationService`
- ✅ **Exception Handling**: Global exception handler integration

### Pending Integrations
- ⏳ **Booking Module**: Update booking status after payment
- ⏳ **Ticket Module**: Generate tickets after successful payment
- ⏳ **Email Module**: Send confirmation/failure/refund emails
- ⏳ **Payment Gateways**: Stripe, PayPal, Mobile Money APIs
- ⏳ **Webhook System**: Process async gateway callbacks

---

## Quick Start Guide

### 1. Test Payment Flow Manually

Use Bruno test files in `http/Payment/`:

```bash
# 1. Initiate payment
POST {{host}}/api/v1/payments/initiate
Body: { bookingId, amount, currency, paymentMethod, paymentGateway }

# 2. Confirm payment (simulate webhook)
POST {{host}}/api/v1/payments/confirm
Body: { paymentReference, transactionId }

# 3. Check status
GET {{host}}/api/v1/payments/status/{paymentReference}

# 4. View user payments
GET {{host}}/api/v1/payments/my-payments?page=0&size=10
```

### 2. Database Setup

Payment table will be auto-created by Hibernate. Recommended indexes:

```sql
CREATE INDEX idx_payment_user_id ON payments(user_id);
CREATE INDEX idx_payment_booking_id ON payments(booking_id);
CREATE INDEX idx_payment_reference ON payments(payment_reference);
CREATE INDEX idx_payment_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_created_at ON payments(created_at);
```

### 3. Environment Variables

Add to your `.env` or environment:

```bash
# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# PayPal
PAYPAL_CLIENT_ID=...
PAYPAL_CLIENT_SECRET=...

# Email
EMAIL_USERNAME=noreply@events.com
EMAIL_PASSWORD=...
```

---

## Architecture Compliance

The payment module follows all architectural rules:

- ✅ **Result Pattern**: All controller methods return `Result<T>`
- ✅ **DTO Pattern**: All DTOs are records ending with "Dto"
- ✅ **Service Layer**: `@Transactional` service that returns DTOs
- ✅ **Repository Pattern**: Interface starts with "I"
- ✅ **MapStruct**: Mapper in `..dto.mapper..` package
- ✅ **Enum Naming**: All enums end with "Enum"
- ✅ **Exception Handling**: Custom exceptions with global handler
- ✅ **Soft Delete**: Extends `AuditableEntity` → `BaseEntity`
- ✅ **Auditing**: Automatic timestamp and user tracking

All ArchUnit tests pass successfully.

---

## Key Files Reference

```
payment/
├── controller/
│   └── PaymentController.java (9 endpoints)
├── dto/
│   ├── InitiatePaymentDto.java
│   ├── ConfirmPaymentDto.java
│   ├── RefundPaymentDto.java
│   ├── GetPaymentDto.java
│   ├── PaymentResponseDto.java
│   └── mapper/
│       └── IPaymentMapper.java
├── entity/
│   └── Payment.java
├── enumeration/
│   ├── PaymentStatusEnum.java (6 states)
│   ├── PaymentMethodEnum.java (7 methods)
│   └── PaymentGatewayEnum.java (7 gateways)
├── exception/
│   ├── PaymentNotFoundException.java
│   ├── PaymentFailedException.java
│   ├── InvalidPaymentStatusException.java
│   └── PaymentAlreadyProcessedException.java
├── repository/
│   └── IPaymentRepository.java (7 custom queries)
└── service/
    ├── IPaymentService.java (9 methods)
    └── impl/
        └── PaymentService.java
```

---

## Support & Questions

For questions or issues related to the Payment module:

1. Check the TODOs in `PaymentService.java` (lines 107-109, 210-213)
2. Review the test files in `http/Payment/`
3. Refer to `docs/PAYMENT-MODULE-COMPLETION.md` for detailed implementation report
4. Check CLAUDE.md for architecture guidelines

---

**Last Updated**: 2026-04-21
**Module Status**: Core implementation complete, pending integrations
**Architecture Tests**: All passing ✅
