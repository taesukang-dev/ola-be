package com.example.ola.dto.request;

import lombok.Getter;

public enum CommentWriteRequestType {
    TEAM_POST("teamPost"), POST("post");

    @Getter private String name;

    CommentWriteRequestType(String name) {
        this.name = name;
    }
}
