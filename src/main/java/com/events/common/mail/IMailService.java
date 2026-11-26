package com.events.common.mail;

import java.util.Map;

public interface IMailService {
    void sendVerificationEmail(String email, String verificationToken);

    void sendResetPasswordEmail(String email, String token);

    /**
     * Envoie la confirmation de commande avec un QR code par billet (en pièce jointe).
     * @param ticketQrCodes code du billet -> image PNG du QR code
     */
    void sendBookingConfirmationEmail(String email, String fullName, String eventName,
                                      String bookingReference, Map<String, byte[]> ticketQrCodes);
}
