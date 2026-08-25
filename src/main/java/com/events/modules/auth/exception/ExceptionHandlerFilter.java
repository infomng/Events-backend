package com.events.modules.auth.exception;

import com.events.common.result.Result;
import com.events.common.utils.contants.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {

            sendProblemDetailResponse(
                    HttpStatus.UNAUTHORIZED,
                    e.getClass().getSimpleName(),
                    Constants.INVALID_TOKEN,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED);
        }
        catch (TooManyRequestException e) {

            sendProblemDetailResponse(
                    HttpStatus.TOO_MANY_REQUESTS,
                    e.getClass().getSimpleName(),
                    Constants.TOO_MANY_REQUESTS,
                    response,
                    HttpStatus.TOO_MANY_REQUESTS.value());
        }
        catch (RuntimeException e) {
            sendProblemDetailResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getClass().getSimpleName(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    response,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private static void sendProblemDetailResponse(HttpStatus tooManyRequests, String exceptionClassName, String message, HttpServletResponse response, int statusCode) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatus(tooManyRequests);
        problemDetail.setTitle(exceptionClassName);
        problemDetail.setDetail(message);
        problemDetail.setProperty(Constants.TIMESTAMP, Instant.now().toString());

        var result = new ObjectMapper().writeValueAsString(Result.failure(problemDetail));

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(result);
    }
}