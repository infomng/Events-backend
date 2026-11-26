package com.events.modules.ticket.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

/**
 * Signe et vérifie le contenu des QR codes des billets.
 * Format : {@code EVT1.<ticketCode>.<signature HMAC-SHA256 en base64url>}.
 * Sans la clé, il est impossible de fabriquer un QR code valide, même en connaissant un code de billet.
 */
@Component
public class TicketTokenSigner {

    private static final String PREFIX = "EVT1";
    private static final String ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_LENGTH = 32;

    private final SecretKeySpec key;

    public TicketTokenSigner(@Value("${ticket.signing-secret}") String secret) {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("ticket.signing-secret must be at least " + MIN_SECRET_LENGTH + " characters");
        }
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public String sign(String ticketCode) {
        return PREFIX + "." + ticketCode + "." + signature(ticketCode);
    }

    /**
     * @return le code du billet si la signature est valide, vide sinon
     */
    public Optional<String> verify(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String[] parts = token.trim().split("\\.");
        if (parts.length != 3 || !PREFIX.equals(parts[0]) || parts[1].isBlank()) {
            return Optional.empty();
        }
        byte[] expected = signature(parts[1]).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = parts[2].getBytes(StandardCharsets.US_ASCII);
        // Comparaison à temps constant : pas d'attaque par mesure du temps de réponse
        return MessageDigest.isEqual(expected, actual) ? Optional.of(parts[1]) : Optional.empty();
    }

    private String signature(String ticketCode) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            byte[] raw = mac.doFinal(ticketCode.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to sign ticket", e);
        }
    }
}
