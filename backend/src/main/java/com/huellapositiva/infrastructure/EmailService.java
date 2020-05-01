package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.Email;

public interface EmailService {

    void sendEmail (Email email);
}
