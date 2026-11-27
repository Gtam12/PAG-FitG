package com.fitg.common.web;


import com.fitg.auth.exception.EmailAlreadyUsedException;
import com.fitg.auth.exception.InvalidCredentialsException;
import com.fitg.user.exception.UserNotFoundException;
import jakarta.validation.constraints.Email;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(EmailAlreadyUsedException.class)
    ProblemDetail handleEmailUsed(EmailAlreadyUsedException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getLocalizedMessage());
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getLocalizedMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException e){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getLocalizedMessage());
    }

}
