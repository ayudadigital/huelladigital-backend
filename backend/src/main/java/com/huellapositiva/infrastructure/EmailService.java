package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.model.valueobjects.Email;

public interface EmailService {

    void sendEmail (Email email);
}
