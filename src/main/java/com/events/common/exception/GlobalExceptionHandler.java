package com.events.common.exception;

import com.events.common.result.EmptyResult;
import com.events.common.result.Result;
import com.events.common.utils.contants.Constants;
import com.events.modules.auth.exception.EmailAlreadyExistException;
import com.events.modules.auth.exception.ForbidenException;
import com.events.modules.auth.exception.TooManyRequestException;
import com.events.modules.auth.exception.UnauthorizedException;
import com.events.modules.auth.exception.UserNotFoundException;
import com.events.modules.auth.exception.UserNotVerifiedException;
import com.events.modules.booking.exception.BookingNotFoundException;
import com.events.modules.cart.exception.CartItemNotFoundException;
import com.events.modules.cart.exception.CartNotFoundException;
import com.events.modules.cart.exception.NotEnoughQuantityException;
import com.events.modules.cart.exception.TooMuchQuantityException;
import com.events.modules.category.exception.CategoryInUseException;
import com.events.modules.category.exception.CategoryNotFoundException;
import com.events.modules.country.exception.CountryAlreadyExistsException;
import com.events.modules.country.exception.CountryInUseException;
import com.events.modules.country.exception.CountryNotFoundException;
import com.events.modules.event.exception.EventNotFoundException;
import com.events.modules.event.exception.MaximumNumberOfImageException;
import com.events.modules.event.exception.PriceCategoryNotFoundException;
import com.events.modules.event.exception.TicketsSoldOutException;
import com.events.modules.favorite.exception.FavoriteAlreadyExistsException;
import com.events.modules.favorite.exception.FavoriteMaximumSizeReachedException;
import com.events.modules.favorite.exception.FavoriteNotFoundException;
import com.events.modules.payment.exception.InvalidPaymentStatusException;
import com.events.modules.payment.exception.PaymentAlreadyProcessedException;
import com.events.modules.payment.exception.PaymentFailedException;
import com.events.modules.payment.exception.PaymentNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler({BadRequestException.class, BusinessException.class})
    public ResponseEntity<Result<ProblemDetail>> handleRuntime(RuntimeException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle(ex.getClass().getSimpleName());
        detail.setDetail(ex.getMessage());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(detail));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<EmptyResult> handleWebClientResponseException(WebClientResponseException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(ex.getStatusCode());
        detail.setTitle("Erreur WebClient");
        detail.setDetail(ex.getMessage() + Constants.EMPTY_STRING + ex.getResponseBodyAsString());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EmptyResult> handleInternalServerError(Exception ex) {
        // Le message d'une erreur inattendue peut contenir du SQL, des noms de tables ou des données :
        // il est journalisé côté serveur, jamais renvoyé au client.
        log.error("Unhandled exception", ex);
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        detail.setTitle("Erreur interne du serveur");
        detail.setDetail("Une erreur inattendue est survenue. Réessayez plus tard.");
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EmptyResult> handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Erreur de validation");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        detail.setDetail("Les données fournies sont invalides.");
        detail.setProperty("errors", errors);
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<EmptyResult> handleUserNotFound(UsernameNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle("Utilisateur introuvable");
        detail.setDetail(ex.getMessage());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler({EventNotFoundException.class,
            CategoryNotFoundException.class,
            CountryNotFoundException.class,
            CartNotFoundException.class,
            CartItemNotFoundException.class,
            PaymentNotFoundException.class,
            PriceCategoryNotFoundException.class,
            BookingNotFoundException.class,
            UserNotFoundException.class,
            ResourceNotFoundException.class})
    public ResponseEntity<EmptyResult> handleNotFound(RuntimeException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle(ex.getClass().getSimpleName());
        detail.setDetail(ex.getMessage());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler({CategoryInUseException.class, CountryInUseException.class, CountryAlreadyExistsException.class,
            FavoriteAlreadyExistsException.class, FavoriteMaximumSizeReachedException.class,
            NotEnoughQuantityException.class, TooMuchQuantityException.class, MaximumNumberOfImageException.class,
            TicketsSoldOutException.class,
            EmailAlreadyExistException.class,
            PaymentAlreadyProcessedException.class, InvalidPaymentStatusException.class, PaymentFailedException.class})
    public ResponseEntity<EmptyResult> handleInUse(RuntimeException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle(ex.getClass().getSimpleName());
        detail.setDetail(ex.getMessage());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<EmptyResult> handleUnauthorized(RuntimeException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex);
    }

    // AccessDeniedException : levée par @PreAuthorize, sans ce handler elle finirait en 500
    @ExceptionHandler({ForbidenException.class, AccessDeniedException.class, UserNotVerifiedException.class})
    public ResponseEntity<EmptyResult> handleForbidden(RuntimeException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<EmptyResult> handleTooManyRequests(TooManyRequestException ex) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex);
    }

    private static ResponseEntity<EmptyResult> buildResponse(HttpStatus status, RuntimeException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setTitle(ex.getClass().getSimpleName());
        detail.setDetail(ex.getMessage());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(status)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    public ResponseEntity<EmptyResult> handleFavoriteNotFound(FavoriteNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle("Favorite not found");
        detail.setDetail(ex.getMessage());
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(EmptyResult.failure(detail));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<EmptyResult> handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Validation échouée");
        detail.setDetail("Certaines contraintes de validation ont échoué.");
        detail.setProperty(Constants.TIMESTAMP, Instant.now().toString());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String path = cv.getPropertyPath().toString();
            String message = cv.getMessage();
            errors.put(path, message);
        });

        detail.setProperty("violations", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmptyResult.failure(detail));
    }
}

