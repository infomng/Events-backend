package com.events.modules.payment.service;

import com.events.common.config.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class StripeGatewayService {

    private final StripeConfig stripeConfig;

    public PaymentIntent createPaymentIntent(Long amount, String currency, String description, String orderId) throws StripeException {
        PaymentIntentCreateParams.Builder createParamsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amount) // amount in cents
                .setCurrency(currency)
                .setDescription(description);

        // Optional: Add metadata
        if (orderId != null) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("order_id", orderId);
            createParamsBuilder.setMetadata(metadata);
        }

        PaymentIntentCreateParams createParams = createParamsBuilder.build();

        return PaymentIntent.create(createParams);
    }
    
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.confirm();
    }
}
