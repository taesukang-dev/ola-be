package com.example.ola.domain;

import lombok.Getter;

public enum AlarmType {
    COMMENT("New Comment"), JOIN("New Joined Member"),;

    @Getter private String name;

    AlarmType(String name) {
        this.name = name;
    }
}
