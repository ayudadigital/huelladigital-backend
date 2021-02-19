package com.huellapositiva.infrastructure;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.huellapositiva.domain.model.valueobjects.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "huellapositiva.feature.email.enabled", havingValue = "true")
public class AwsEmailService implements EmailService{

    @Autowired
    private AmazonSimpleEmailService awsSesClient;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(emailMessage.getTo()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(emailMessage.getBody())))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(emailMessage.getSubject())))
                    .withSource(emailMessage.getFrom());
            awsSesClient.sendEmail(request);
    }
}
