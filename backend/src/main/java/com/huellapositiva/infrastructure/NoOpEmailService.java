package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.Email;
import org.springframework.stereotype.Service;

@Service
public class NoOpEmailService implements EmailService {
    @Override
    public boolean sendEmail(Email email) {
        return true;
    }
}
