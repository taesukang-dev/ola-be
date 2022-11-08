package com.example.ola.domain;

import lombok.Getter;

public enum UserGender {
    M("male"), F("female");

    @Getter private String name;

    UserGender(String name) {
        this.name = name;
    }
}
