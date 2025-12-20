package com.events.common.enums;

public enum UrlsEnum {
    LOGIN("/auth/login"),
    REGISTER("/auth/register"),
    DASHBOARD("/auth/dashboard"),
    PROFILE("/auth/profile"),
    LOGOUT("/auth/logout"),
    VERIFY_EMAIL("/auth/verify-email"),
    RESET_PASSWORD("/auth/reset-password");

    private final String path;

    UrlsEnum(String path) {
        this.path = "/api/v1" + path;
    }

    public String getPath() {
        return path;
    }
}
