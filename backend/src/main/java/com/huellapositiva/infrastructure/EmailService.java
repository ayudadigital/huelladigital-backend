package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.model.valueobjects.EmailMessage;

public interface EmailService {

    void sendEmail (EmailMessage emailMessage);
}
