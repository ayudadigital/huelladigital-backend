package com.huellapositiva.application.exception.handler;

import com.huellapositiva.application.dto.ErrorResponseDto;
import com.huellapositiva.application.exception.InvalidFieldException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@ControllerAdvice
public class BadRequestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MESSAGE_FORMAT = "Field: %s %s";


    @ExceptionHandler(InvalidFieldException.class)
    public final ResponseEntity<ErrorResponseDto> handleInvalidFieldException(InvalidFieldException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponseDto error = new ErrorResponseDto("Validation Failed", details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> details = new ArrayList<>();
        for(ObjectError error : ex.getBindingResult().getAllErrors()) {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                details.add(format(MESSAGE_FORMAT, fieldError.getField(), fieldError.getDefaultMessage()));
            } else {
                details.add(format(MESSAGE_FORMAT, requireNonNull(error.getArguments())[0], error.getDefaultMessage()));
            }
        }
        ErrorResponseDto error = new ErrorResponseDto("Validation Failed", details);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}