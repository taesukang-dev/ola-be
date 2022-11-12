package com.example.ola.dto.request;

import lombok.Getter;

public enum PostType {
    TEAM_POST("teamPost"), POST("post");

    @Getter private String name;

    PostType(String name) {
        this.name = name;
    }
}
