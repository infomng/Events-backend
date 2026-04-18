package com.events.modules.favorite.exception;

import com.events.common.utils.contants.Constants;

public class FavoriteAlreadyExistsException extends RuntimeException {
    public FavoriteAlreadyExistsException() {
        super(Constants.FAVORITE_ALREADY_EXISTS);
    }
}
