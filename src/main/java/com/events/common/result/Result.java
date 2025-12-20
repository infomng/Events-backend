package com.events.common.result;

import jakarta.annotation.Nullable;
import org.springframework.http.ProblemDetail;

public class Result<T> {
    public boolean isSuccess;
    public ProblemDetail error;
    private final T value;

    public Result(boolean isSuccess, @Nullable ProblemDetail error, @Nullable T data) {

        if (isSuccess && error != null) {
            throw new IllegalArgumentException("A successful result must not have an error.");
        }
        if (!isSuccess && error == null) {
            throw new IllegalArgumentException("A failed result must contain an error.");
        }


        this.isSuccess = isSuccess;
        this.error = error;
        this.value = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, null, data);
    }

    public static <T> Result<T> success() {
        return new Result<>(true, null, null);
    }


    public static  <T> Result<T> failure(ProblemDetail error) {
        return new Result<>(false, error, null);
    }

    @Nullable
    public T getValue() {
        return value;
    }
}
