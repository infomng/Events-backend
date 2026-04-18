package com.events.modules.favorite.exception;

import com.events.common.utils.contants.Constants;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException() {
        super(Constants.FAVORITE_NOT_FOUND);
    }
}
