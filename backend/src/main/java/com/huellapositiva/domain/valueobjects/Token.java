package com.huellapositiva.domain.valueobjects;

import java.util.UUID;

public class Token {
    private UUID token;

    private Token(UUID token) {
        this.token = token;
    }

    public static Token createToken() {
        return new Token(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return token.toString();
    }
}
