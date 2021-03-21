package com.huellapositiva.infrastructure;

import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "huellapositiva.feature.email.enabled", havingValue = "true")
public class EmailConfig {

    @Autowired
    private AwsSesProperties awsSesProperties;

    @Bean
    @Profile({"dev", "prod"})
    public AmazonSimpleEmailService getAwsSesClient() {
        log.info("AWS SES client enabled");
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(new EC2ContainerCredentialsProviderWrapper())
                .withRegion(awsSesProperties.getRegion())
                .build();
    }

    @Bean
    @Profile("!dev & !dev-alt & !prod")
    public AmazonSimpleEmailService getLocalstackAwsSesClient() {
        log.info("Localstack AWS SES client enabled");
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsSesProperties.getEndpoint(), awsSesProperties.getRegion()))
                .build();
    }
}
