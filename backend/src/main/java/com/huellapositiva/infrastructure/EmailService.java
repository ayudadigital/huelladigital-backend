package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.Email;

public interface EmailService {

    boolean sendEmail (Email email);
}
