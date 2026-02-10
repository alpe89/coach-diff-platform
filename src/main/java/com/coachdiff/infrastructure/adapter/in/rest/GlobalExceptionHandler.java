package com.coachdiff.infrastructure.adapter.in.rest;

import com.coachdiff.domain.exception.ErrorCode;
import com.coachdiff.domain.exception.SummonerProfileNotFoundException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotAuthenticationException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotException;
import com.coachdiff.infrastructure.adapter.out.exception.RiotRateLimitException;
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

  @ExceptionHandler(RiotException.class)
  public ResponseEntity<ApiError> riotExceptionHandler(RiotException ex) {
    int statusCode = ex.getStatus();

    return switch (ex) {
      case RiotAuthenticationException e ->
          ResponseEntity.status(HttpStatus.valueOf(statusCode))
              .body(new ApiError(statusCode, ErrorCode.RIOT_API_ERROR.name(), e.getMessage()));
      case RiotRateLimitException e ->
          ResponseEntity.status(HttpStatus.valueOf(statusCode))
              .body(new ApiError(statusCode, ErrorCode.RIOT_API_RATE_LIMIT.name(), e.getMessage()));
      default ->
          ResponseEntity.status(HttpStatus.valueOf(statusCode))
              .body(new ApiError(statusCode, ErrorCode.UNEXPECTED_ERROR.name(), ex.getMessage()));
    };
  }
}
