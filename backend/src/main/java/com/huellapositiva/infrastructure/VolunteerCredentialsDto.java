package com.huellapositiva.infrastructure;

public class VolunteerCredentialsDto {
    private final String email;
    private final String password;

    public VolunteerCredentialsDto() {
        this.email = "";
        this.password = "";
    }

    public VolunteerCredentialsDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}
