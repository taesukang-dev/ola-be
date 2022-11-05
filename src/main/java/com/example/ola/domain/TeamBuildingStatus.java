package com.example.ola.domain;

import lombok.Getter;

public enum TeamBuildingStatus {
    READY("READY"), CONFIRMED("CONFIRMED"), CANCELED("CANCELED");
    @Getter private String name;

    TeamBuildingStatus(String name) {
        this.name = name;
    }
}
