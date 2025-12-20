package com.events.modules.auth.exception;

public class InvalidRefreshTokenException extends UnauthorizedException {
    public InvalidRefreshTokenException() {
        super("Invalid refresh token. Please login again.");
    }
}
