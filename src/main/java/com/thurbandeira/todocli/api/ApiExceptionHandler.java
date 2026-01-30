package com.thurbandeira.todocli.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "requisicao invalida" : error.getDefaultMessage())
                .orElse("requisicao invalida");
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail(message);
        return detail;
    }
}
