package com.events.modules.favorite.exception;

public class FavoriteMaximumSizeReachedException extends RuntimeException {
    public FavoriteMaximumSizeReachedException() {
        super("You have reached the maximum number of favorites (30).");
    }
}
