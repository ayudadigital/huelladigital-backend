package com.huellapositiva.infrastructure;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
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
                .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                .withRegion(awsSesProperties.getRegion())
                .build();
    }

    @Bean
    @Profile("!dev & !prod")
    public AmazonSimpleEmailService getLocalstackAwsSesClient() {
        log.info("Localstack AWS SES client enabled");
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsSesProperties.getAccessKey(), awsSesProperties.getSecretKey());
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsSesProperties.getEndpoint(), awsSesProperties.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

}
