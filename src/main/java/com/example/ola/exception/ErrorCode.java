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
    UNAUTHORIZED_BEHAVIOR(HttpStatus.UNAUTHORIZED, "Unauthorized behavior"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "Post not found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment not found"),
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "Alarm not found"),
    ALARM_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Connecting alarm occurs Error"),
    INVALID_KEYWORD(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid keyword"),
    ;

    private HttpStatus status;
    private String message;
}
