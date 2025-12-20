package com.events.modules.auth.exception;

import com.events.common.utils.contants.Constants;

public class TooManyRequestException extends RuntimeException {
    public TooManyRequestException() {
        super(Constants.TOO_MANY_REQUESTS);
    }
}
