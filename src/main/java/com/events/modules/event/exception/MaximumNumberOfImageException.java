package com.events.modules.event.exception;

public class MaximumNumberOfImageException extends RuntimeException {
    public MaximumNumberOfImageException(int maxImages) {
        super("Maximum number of images " + maxImages + " reached for this event");
    }
}
