package com.events.common.enums;

import lombok.Getter;

@Getter
public enum UrlsEnum {
    VERIFY_EMAIL("/verify-email"),
    RESET_PASSWORD_FRONT_END_PATH("/reset-password");

    private final String path;

    UrlsEnum(String path) {
        this.path = path;
    }

}
