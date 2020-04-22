package com.huellapositiva.domain.valueobjects;

import java.util.UUID;

public class Token {
    private UUID value;

    private Token(UUID token) {
        this.value = token;
    }

    public static Token createToken() {
        return new Token(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
