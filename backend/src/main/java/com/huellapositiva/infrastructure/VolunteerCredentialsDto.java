package com.huellapositiva.infrastructure;

public class VolunteerCredentialsDto {
    private String email;
    private String password;

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
