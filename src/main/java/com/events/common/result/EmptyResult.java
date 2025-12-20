package com.events.common.result;

import jakarta.annotation.Nullable;
import org.springframework.http.ProblemDetail;

public class EmptyResult {
    public boolean isSuccess;
    public ProblemDetail error;

    public EmptyResult(boolean isSuccess, @Nullable ProblemDetail error) {
        if (isSuccess && error != null) {
            throw new IllegalArgumentException("A successful result must not have an error.");
        }
        if (!isSuccess && error == null) {
            throw new IllegalArgumentException("A failed result must contain an error.");
        }


        this.isSuccess = isSuccess;
        this.error = error;
    }

    public static EmptyResult success() {
        return new EmptyResult(true, null);
    }

    public static EmptyResult failure(ProblemDetail error) {
        return new EmptyResult(false, error);
    }
}

