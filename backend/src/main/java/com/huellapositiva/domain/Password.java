package com.huellapositiva.domain;

import jdk.jshell.spi.ExecutionControl;

public class Password {
    private String hashedPassword;
    private String password;

    private Password(String password, String hashedPassword) {
        this.password = password;
        this.hashedPassword = hashedPassword;
    }

    public static Password from(String password, String hashedPassword) {
        // TODO: validate password security requiments
        return new Password(password, hashedPassword);
    }

    public String hash() {
        return hashedPassword;
    }
}
