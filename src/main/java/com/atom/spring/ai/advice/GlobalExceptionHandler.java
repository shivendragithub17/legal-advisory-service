package com.atom.spring.ai.advice;

import com.atom.spring.ai.model.ErrorModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(value = {Exception.class, RuntimeException.class})
  public ResponseEntity<ErrorModel> handleGlobalException(Exception ex) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.internalServerError()
        .body(
            ErrorModel.builder()
                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .message(ex.getMessage())
                .details(ex.getLocalizedMessage())
                .build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorModel> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.badRequest()
        .body(
            ErrorModel.builder()
                .code(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .message(ex.getMessage())
                .details(ex.getLocalizedMessage())
                .build());
  }
}
