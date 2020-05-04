package com.huellapositiva.domain;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;

public class FailEmailConfirmation {
    private EmailConfirmation emailConfirmation;

    public FailEmailConfirmation(EmailConfirmation emailConfirmation) {
        this.emailConfirmation = emailConfirmation;
    }

    public String getEmailAddress() {
        return emailConfirmation.getEmailAddress();
    }
}
