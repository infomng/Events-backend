package com.events.common.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for handling internationalization (i18n) of messages.
 * Uses Spring's MessageSource to retrieve localized messages based on the current locale.
 */
@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final MessageSource messageSource;

    /**
     * Get localized message using the current locale from LocaleContextHolder.
     *
     * @param messageKey The message key from properties file
     * @param args Optional arguments to substitute in the message
     * @return Localized message
     */
    public String getMessage(String messageKey, Object... args) {
        return messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized message with specific locale.
     *
     * @param messageKey The message key from properties file
     * @param locale The locale to use
     * @param args Optional arguments to substitute in the message
     * @return Localized message
     */
    public String getMessage(String messageKey, Locale locale, Object... args) {
        return messageSource.getMessage(messageKey, args, locale);
    }

    /**
     * Get localized message with default fallback.
     *
     * @param messageKey The message key from properties file
     * @param defaultMessage Default message if key not found
     * @param args Optional arguments to substitute in the message
     * @return Localized message or default if key not found
     */
    public String getMessageOrDefault(String messageKey, String defaultMessage, Object... args) {
        return messageSource.getMessage(messageKey, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get current locale from request context.
     *
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}
