package com.huellapositiva.domain.valueobjects;

public class PasswordHash {
    private final String hash;

    public PasswordHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }
}
