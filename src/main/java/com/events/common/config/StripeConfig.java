package com.events.common.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeConfig {

    private String secretKey;
    private String publishableKey;
    private String webhookSecret;
    /** Devise unique utilisée pour les paiements (code ISO 4217). */
    private String currency = "usd";

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
