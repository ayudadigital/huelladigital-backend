package com.huellapositiva.domain;

public class Password {
    private String password;

    private Password(String password) {
        this.password = password;
    }

    public static Password from(String password) {
        // TODO: validate password security requiments
        return new Password(password);
    }

    @Override
    public String toString() {
        return password;
    }
}

