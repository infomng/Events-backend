package com.events.common.mail;

import com.events.common.config.properties.AppProperties;
import com.events.common.config.properties.MailProperties;
import com.events.common.enums.UrlsEnum;
import com.events.common.utils.contants.Constants;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;

import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;

@Service
@RequiredArgsConstructor
@Transactional
public class MailService implements IMailService {
    private final JavaMailSender mailSender;

    private final AppProperties appProperties;
    private final MailProperties mailProperties;



    @Async
    @Override
    public void sendVerificationEmail(String email, String verificationToken) {
        String path = UrlsEnum.VERIFY_EMAIL.getPath();
        sendEmail(
                email,
                verificationToken,
                Constants.EMAIL_VERIFICATION,
                path,
                Constants.CLICK_THE_BUTTON_BELOW_TO_VERIFY_YOUR_EMAIL_ADDRESS);
    }

    @Async
    @Override
    public void sendResetPasswordEmail(String email, String token) {
        String path = UrlsEnum.RESET_PASSWORD_FRONT_END_PATH.getPath();
        sendEmail(
                email,
                token,
                Constants.PASSWORD_RESET_REQUEST,
                path,
                Constants.CLICK_THE_BUTTON_BELOW_TO_RESET_YOUR_PASSWORD);
    }

    @Async
    @Override
    public void sendBookingConfirmationEmail(String email, String fullName, String eventName,
                                             String bookingReference, Map<String, byte[]> ticketQrCodes) {
        try {
            StringBuilder ticketList = new StringBuilder();
            ticketQrCodes.keySet().forEach(code ->
                    ticketList.append("<li style=\"font-family: monospace;\">")
                            .append(HtmlUtils.htmlEscape(code))
                            .append("</li>"));

            String ticketsUrl = appProperties.frontendUrl() + "/tickets";
            String content = Constants.BOOKING_CONFIRMATION_EMAIL_CONTENT.formatted(
                    HtmlUtils.htmlEscape(fullName == null ? "" : fullName),
                    HtmlUtils.htmlEscape(eventName == null ? "" : eventName),
                    HtmlUtils.htmlEscape(bookingReference),
                    ticketQrCodes.size(),
                    ticketList,
                    ticketsUrl
            );

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(email);
            helper.setSubject(Constants.BOOKING_CONFIRMATION_SUBJECT.formatted(bookingReference));
            helper.setFrom(mailProperties.username());
            helper.setText(content, true);
            for (Map.Entry<String, byte[]> ticket : ticketQrCodes.entrySet()) {
                if (ticket.getValue() != null) {
                    helper.addAttachment("ticket-" + ticket.getKey() + ".png",
                            new ByteArrayResource(ticket.getValue()), "image/png");
                }
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            LOGGER.error(Constants.FAILED_TO_SEND_EMAIL, e.getMessage(), e);
        }
    }

    private void sendEmail(String email, String token, String subject, String path, String message) {
        try {
            String actionUrl = appProperties.frontendUrl() + path + "?token=" + token;

            String content = Constants.SEND_VERIFICATION_EMAIL_CONTENT.formatted(subject, message, actionUrl, actionUrl);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(mailProperties.username());
            helper.setText(content, true);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            LOGGER.error(Constants.FAILED_TO_SEND_EMAIL, e.getMessage(), e);
        }
    }
}
