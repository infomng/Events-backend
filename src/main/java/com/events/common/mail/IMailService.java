package com.events.common.mail;

public interface IMailService {
    void sendVerificationEmail(String email, String verificationToken);

    void sendResetPasswordEmail(String email, String token);
}
