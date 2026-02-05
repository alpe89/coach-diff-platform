package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(SummonerProfileNotFoundException.class)
  public ResponseEntity<ApiError> summonerProfileNotFoundHandler(
      SummonerProfileNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError(HttpStatus.NOT_FOUND.value(), ex.getCode(), ex.getMessage()));
  }
}
