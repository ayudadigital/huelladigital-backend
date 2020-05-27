package com.huellapositiva.infrastructure;

import java.util.ArrayList;

public class VolunteerCredentialsDto {
    private String email;
    private String password;
    private ArrayList<String> credentials;

    public VolunteerCredentialsDto() {
        this.email = "";
        this.password = "";
        this.credentials = null;
    }

    public VolunteerCredentialsDto(String email, String password, ArrayList<String> credentials) {
        this.email = email;
        this.password = password;
        this.credentials = credentials;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getCredentials() {
        return credentials;
    }
}
