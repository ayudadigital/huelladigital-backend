package com.huellapositiva.domain;

public class PasswordHash {
    private String hash;

    public PasswordHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }
}
