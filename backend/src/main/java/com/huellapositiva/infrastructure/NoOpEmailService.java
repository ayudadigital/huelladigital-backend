package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.PhysicalEmail;
import org.springframework.stereotype.Service;

@Service
public class NoOpEmailService implements EmailService {
    @Override
    public boolean sendEmail(PhysicalEmail email) {
        return true;
    }
}
