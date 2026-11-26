package com.events.modules.payment.service;

import com.events.common.config.StripeConfig;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class StripeGatewayService {

    private final StripeConfig stripeConfig;

    /**
     * Crée un PaymentIntent. La référence de paiement sert de clé d'idempotence :
     * un double appel ne crée pas deux intentions de paiement.
     */
    public PaymentIntent createPaymentIntent(Long amount, String currency, String description, String orderId) throws StripeException {
        PaymentIntentCreateParams.Builder createParamsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amount) // amount in cents
                .setCurrency(currency)
                .setDescription(description)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                );

        RequestOptions.RequestOptionsBuilder options = RequestOptions.builder();
        if (orderId != null) {
            createParamsBuilder.putMetadata("order_id", orderId);
            options.setIdempotencyKey("pi-" + orderId);
        }

        return PaymentIntent.create(createParamsBuilder.build(), options.build());
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId).cancel();
    }

    /**
     * Rembourse intégralement un PaymentIntent. Idempotent par paiement.
     */
    public Refund refundPaymentIntent(String paymentIntentId, String idempotencyKey) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();
        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();
        return Refund.create(params, options);
    }

    /**
     * Vérifie la signature Stripe d'un webhook et retourne l'événement.
     */
    public Event constructWebhookEvent(String payload, String signatureHeader) throws SignatureVerificationException {
        String webhookSecret = stripeConfig.getWebhookSecret();
        if (!StringUtils.hasText(webhookSecret)) {
            throw new IllegalStateException("stripe.webhook-secret is not configured");
        }
        return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    }

    public String getCurrency() {
        return stripeConfig.getCurrency().toLowerCase();
    }
}
