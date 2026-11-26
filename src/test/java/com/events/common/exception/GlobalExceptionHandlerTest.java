package com.events.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void unexpectedErrorDoesNotLeakItsMessageToTheClient() {
        var response = new GlobalExceptionHandler().handleInternalServerError(
                new IllegalStateException("could not execute statement [insert into events ...]"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error.getDetail()).doesNotContain("insert", "events", "statement");
    }
}
