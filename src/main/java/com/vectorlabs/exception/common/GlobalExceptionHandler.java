package com.vectorlabs.exception.common;

import com.vectorlabs.dto.error.ErrorAnswer;
import com.vectorlabs.dto.error.UnitError;
import com.vectorlabs.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorAnswer handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<UnitError> listErrors = e.getFieldErrors().stream()
                .map(fe -> new UnitError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ErrorAnswer(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation error!",
                listErrors
        );
    }

    @ExceptionHandler(MissingRequiredFieldException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorAnswer handleMissingRequiredFieldException(MissingRequiredFieldException e) {
        return ErrorAnswer.unprocessableEntity(e.getMessage());
    }

    @ExceptionHandler(ImmutableFieldException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorAnswer handleImmutableFieldException(ImmutableFieldException e) {
        return ErrorAnswer.unprocessableEntity(e.getMessage());
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorAnswer handleOAuth2Auth(OAuth2AuthenticationException e) {
        return  ErrorAnswer.noAuthenticatedAcces(e.getMessage());
    }

    @ExceptionHandler(DoubleRegisterException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorAnswer handleDoubleRegisterException(DoubleRegisterException e) {
        return ErrorAnswer.conflictError(e.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorAnswer handleBusinessRuleException(BusinessRuleException e) {
        return ErrorAnswer.conflictError(e.getMessage());
    }

    @ExceptionHandler(ObjectNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorAnswer handleObjectNotFound(ObjectNotFound e) {
        return ErrorAnswer.notFound(e.getMessage());
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorAnswer handleUnAuthorizedException(UnAuthorizedException e) {
        return new ErrorAnswer(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorAnswer handleBadCredentials(BadCredentialsException ex) {
        return new ErrorAnswer(
                HttpStatus.UNAUTHORIZED.value(), ex.getMessage(),List.of()
        );
    }


    @ExceptionHandler(NoAuthenticatedAcces.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorAnswer handleNoAuthenticatedException(NoAuthenticatedAcces e) {
        return new ErrorAnswer(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorAnswer handleAccessDenied(AccessDeniedException e) {
        return new ErrorAnswer(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                List.of()
        );
    }

    @ExceptionHandler(PaymentRequiredException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ErrorAnswer handlePaymentRequired(PaymentRequiredException ex) {
        return new ErrorAnswer(
                HttpStatus.PAYMENT_REQUIRED.value(),
                ex.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(WebhookRejectedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorAnswer handleWebhookRejected(WebhookRejectedException ex) {
        return new ErrorAnswer(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                List.of()
        );
    }


    @ExceptionHandler(InvalidFieldException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorAnswer handleInvalidFieldException(InvalidFieldException e) {
        return new ErrorAnswer(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                List.of() // Detalhamento do erro
        );
    }

    @ExceptionHandler(ForbiddenAcessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorAnswer handleForbiddenAcess(ForbiddenAcessException e) {
        return new ErrorAnswer(
                HttpStatus.FORBIDDEN.value(),
                e.getMessage(),
                List.of()
        );
    }
}
