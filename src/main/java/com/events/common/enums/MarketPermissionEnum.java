package com.events.common.enums;

public enum MarketPermissionEnum {
    MARKET_READ("0"),
    MARKET_CREATE("1"),
    MARKET_UPDATE("2"),
    MARKET_DELETE("3");

    private final String permission;

    MarketPermissionEnum(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
