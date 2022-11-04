package com.example.ola.domain;

import lombok.Getter;

public enum TeamBuildingStatus {
    READY("READY"), CONFIRMED("CONFIRMED");
    @Getter private String name;

    TeamBuildingStatus(String name) {
        this.name = name;
    }
}
