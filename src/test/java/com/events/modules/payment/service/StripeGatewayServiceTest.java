package com.events.modules.payment.service;

import com.events.common.config.StripeConfig;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeGatewayServiceTest {

    @Mock
    private StripeConfig stripeConfig;

    @InjectMocks
    private StripeGatewayService stripeGatewayService;

    @BeforeEach
    void setUp() {
        // Ensure Stripe.apiKey is set before tests run if @PostConstruct isn't triggered
        when(stripeConfig.getSecretKey()).thenReturn("test_secret_key");
        Stripe.apiKey = stripeConfig.getSecretKey();
    }

    @Test
    void createPaymentIntent_shouldReturnPaymentIntent() throws StripeException {
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            // Given
            Long amount = 1000L; // $10.00
            String currency = "usd";
            String description = "Test Payment";
            String orderId = "order123";

            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setId("pi_test_123");
            mockIntent.setClientSecret("client_secret_test_123");
            mockIntent.setAmount(amount);
            mockIntent.setCurrency(currency);

            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            // When
            PaymentIntent result = stripeGatewayService.createPaymentIntent(amount, currency, description, orderId);

            // Then
            assertNotNull(result);
            assertEquals("pi_test_123", result.getId());
            assertEquals("client_secret_test_123", result.getClientSecret());
            assertEquals(amount, result.getAmount());
            assertEquals(currency, result.getCurrency());

            mockedPaymentIntent.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
        }
    }

//    @Test
//    void createPaymentIntent_shouldThrowStripeException() {
//        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
//            // Given
//            Long amount = 1000L;
//            String currency = "usd";
//            String description = "Test Payment";
//            String orderId = "order123";
//
//            StripeException stripeException = new StripeException("Stripe error") {
//                @Override
//                public String getCode() {
//                    return "api_error";
//                }
//            };
//            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
//                    .thenThrow(stripeException);
//
//            // When / Then
//            StripeException thrown = assertThrows(StripeException.class,
//                    () -> stripeGatewayService.createPaymentIntent(amount, currency, description, orderId));
//            assertEquals("Stripe error", thrown.getMessage());
//        }
//    }

    @Test
    void retrievePaymentIntent_shouldReturnPaymentIntent() throws StripeException {
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            // Given
            String paymentIntentId = "pi_retrieve_456";
            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setId(paymentIntentId);

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockIntent);

            // When
            PaymentIntent result = stripeGatewayService.retrievePaymentIntent(paymentIntentId);

            // Then
            assertNotNull(result);
            assertEquals(paymentIntentId, result.getId());
            mockedPaymentIntent.verify(() -> PaymentIntent.retrieve(paymentIntentId), times(1));
        }
    }

    @Test
    void retrievePaymentIntent_shouldThrowStripeException() {
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            // Given
            String paymentIntentId = "pi_retrieve_456";
            StripeException stripeException = new StripeException() {
                @Override
                public String getCode() {
                    return "not_found";
                }
            };
            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenThrow(stripeException);

            // When / Then
            StripeException thrown = assertThrows(StripeException.class,
                    () -> stripeGatewayService.retrievePaymentIntent(paymentIntentId));
            assertEquals("Retrieve error", thrown.getMessage());
        }
    }

    @Test
    void confirmPaymentIntent_shouldReturnConfirmedPaymentIntent() throws StripeException {
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            // Given
            String paymentIntentId = "pi_confirm_789";
            PaymentIntent mockRetrievedIntent = mock(PaymentIntent.class);
            PaymentIntent mockConfirmedIntent = new PaymentIntent();
            mockConfirmedIntent.setId(paymentIntentId);
            mockConfirmedIntent.setStatus("succeeded");

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockRetrievedIntent);
            when(mockRetrievedIntent.confirm()).thenReturn(mockConfirmedIntent);

            // When
            PaymentIntent result = stripeGatewayService.confirmPaymentIntent(paymentIntentId);

            // Then
            assertNotNull(result);
            assertEquals(paymentIntentId, result.getId());
            assertEquals("succeeded", result.getStatus());
            mockedPaymentIntent.verify(() -> PaymentIntent.retrieve(paymentIntentId), times(1));
            verify(mockRetrievedIntent, times(1)).confirm();
        }
    }

    @Test
    void confirmPaymentIntent_shouldThrowStripeExceptionOnRetrieve() {
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            // Given
            String paymentIntentId = "pi_confirm_789";
            StripeException stripeException = new StripeException("Confirm retrieve error") {
                @Override
                public String getCode() {
                    return "not_found";
                }
            };
            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenThrow(stripeException);

            // When / Then
            StripeException thrown = assertThrows(StripeException.class,
                    () -> stripeGatewayService.confirmPaymentIntent(paymentIntentId));
            assertEquals("Confirm retrieve error", thrown.getMessage());
        }
    }

    @Test
    void confirmPaymentIntent_shouldThrowStripeExceptionOnConfirm() throws StripeException {
        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            // Given
            String paymentIntentId = "pi_confirm_789";
            PaymentIntent mockRetrievedIntent = mock(PaymentIntent.class);
            StripeException stripeException = new StripeException("Confirm error") {
                @Override
                public String getCode() {
                    return "confirm_fail";
                }
            };

            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(paymentIntentId))
                    .thenReturn(mockRetrievedIntent);
            when(mockRetrievedIntent.confirm()).thenThrow(stripeException);

            // When / Then
            StripeException thrown = assertThrows(StripeException.class,
                    () -> stripeGatewayService.confirmPaymentIntent(paymentIntentId));
            assertEquals("Confirm error", thrown.getMessage());
        }
    }
}
