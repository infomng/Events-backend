package com.events.common.utils.contants;

    public final class Constants {

        // Favorites
        public static final String FAVORITE_ALREADY_EXISTS = "Favorite already exist";
        public static final String FAVORITE_NOT_FOUND = "Favorite not found";

        // Events
        public static final String EVENTS_NOT_FOUND = "Event not found";
        public static final String ONLY_EVENTS_WITH_DRAFT_STATUS_CAN_BE_PUBLISHED = "Only events with DRAFT status can be published.";
        public static final String INVALID_DATE = "Start date cannot be after end date";
        public static final String INVALID_START_DATE = "Ticket sales start date cannot be after end date";
        public static final String PRICE_MUST_BE_POSITIVE = "Ticket price must be greater than 0 for paid events";
        public static final String PRICE_MUST_BE_NULL_FOR_FREE_EVENT = "Ticket price must be null for free events";
        public static final String FREE = "FREE";
        public static final String REGULAR = "REGULAR";
        public static final String VIP = "VIP";
        public static final String PREMIUM = "PREMIUM";
        public static final String IS_PUBLIC = "isPublic";
        public static final String IS_FREE_ENTRY = "isFreeEntry";
        public static final String HAS_INVITATION_CODE = "hasInvitationCode";
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";

        // Rate limiting
        public static final String TOO_MANY_REQUESTS = "Too Many Requests";
        public static final int RATE_LIMIT_REQUESTS = 10;
        public static final int RATE_LIMIT_RESET_TIME = 1;

        // Authentication / Authorization
        public static final String TOKEN = "token";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final Long REFRESH_TOKEN_MIN_DURATION = 604800000L;
        public static final String ROLES = "roles";
        public static final String ROLE = "role";
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER = "Bearer ";
        public static final String HTTP_ONLY = "HttpOnly";
        public static final String ANONYMOUS_USER = "anonymousUser";
        public static final String USER_NOT_AUTHENTICATED = "User not authenticated";
        public static final String TOKEN_CANNOT_BE_NULL_OR_EMPTY = "Token cannot be null or empty";
        public static final String INVALID_TOKEN = "Invalid token";

        // User / Account messages
        public static final String EMAIL = "email";
        public static final String FULL_NAME = "fullName";
        public static final String PASSWORD = "password";
        public static final String NAME = "name";
        public static final String USER_ALREADY_VERIFIED = "User already verified";
        public static final String INVALID_VERIFICATION_TOKEN = "Invalid verification token";
        public static final String USER_HAS_BEEN_SUCCESSFULLY_VERIFIED = "User has been successfully verified : ";
        public static final String PASSWORD_HAS_BEEN_RESET_SUCCESSFULLY_FOR = "Password has been reset successfully for ";

        // HTTP / Response keys
        public static final String HEADER = "header";
        public static final String STATUS = "Status";
        public static final String TIMESTAMP = "timestamp";
        public static final String BEARER_PREFIX = BEARER;

        // Email / Notifications
        public static final String FAILED_TO_SEND_EMAIL = "Failed to send email: {}";
        public static final String EMAIL_VERIFICATION = "Email Verification";
        public static final String PLEASE_CHECK_YOUR_INBOX = ". Please check your inbox.";
        public static final String VERIFICATION_EMAIL_SENT_TO = "Verification email sent to ";
        public static final String CLICK_THE_BUTTON_BELOW_TO_VERIFY_YOUR_EMAIL_ADDRESS = "Click the button below to verify your email address:";
        public static final String PASSWORD_RESET_REQUEST = "Password Reset Request";
        public static final String CLICK_THE_BUTTON_BELOW_TO_RESET_YOUR_PASSWORD = "Click the button below to reset your password:";
        public static final String PASSWORD_RESET_EMAIL_SENT_TO = "Password reset email sent to ";
        public static final String SEND_VERIFICATION_EMAIL_CONTENT = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border-radius: 8px; background-color: #f9f9f9; text-align: center;">
                        <h2 style="color: #333;">%s</h2>
                        <p style="font-size: 16px; color: #555;">%s</p>
                        <a href="%s" style="display: inline-block; margin: 20px 0; padding: 10px 20px; font-size: 16px; color: #fff; background-color: #007bff; text-decoration: none; border-radius: 5px;">Proceed</a>
                        <p style="font-size: 14px; color: #777;">Or copy and paste this link into your browser:</p>
                        <p style="font-size: 14px; color: #007bff;">%s</p>
                        <p style="font-size: 12px; color: #aaa;">This is an automated message. Please do not reply.</p>
                    </div>
                """;

        // Misc / Tests
        public static final String EMPTY_STRING = " ";
        public static final String JOHN_DOE = "John Doe";
        public static final String JOHN_DOE_EMAIL = "john.doe@example.com";
        public static final String JOHN_DOE_PASSWORD = "Mvxvxicavxvxcvcsavxvnoestuvxccasa123@#";
        public static final String TEST_MESSAGE = "This is a test message.";

        private Constants() {}
    }