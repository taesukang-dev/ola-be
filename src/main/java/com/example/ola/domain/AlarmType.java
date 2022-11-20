package com.example.ola.domain;

import lombok.Getter;

public enum AlarmType {
    TEAM_COMMENT("New Comment"), JOIN("New Joined Member"),
    WAITING("New Waitng Member"),
    COMMENT("New Comment");

    @Getter private String name;

    AlarmType(String name) {
        this.name = name;
    }
}
