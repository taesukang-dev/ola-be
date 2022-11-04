package com.example.ola.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    DUPLICATED_MEMBER(HttpStatus.CONFLICT, "User name is duplicated"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "invalid password"),
    ;

    private HttpStatus status;
    private String message;
}
