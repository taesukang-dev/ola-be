package com.example.ola.domain;

import lombok.Getter;

public enum UserRole {
    ADMIN("ROLE_ADMIN"), USER("ROLE_USER");
    @Getter private String name;

    UserRole(String name) {
        this.name = name;
    }
}
