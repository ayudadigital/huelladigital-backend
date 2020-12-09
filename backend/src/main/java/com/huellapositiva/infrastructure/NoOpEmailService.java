package com.huellapositiva.infrastructure;

import com.huellapositiva.domain.model.valueobjects.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@ConditionalOnProperty(name = "huellapositiva.feature.email.enabled", havingValue = "false")
public class NoOpEmailService implements EmailService {

    @PostConstruct
    void init() {
        log.info("No operational email service enabled");
    }

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        // Do nothing because it's no operational
    }
}
