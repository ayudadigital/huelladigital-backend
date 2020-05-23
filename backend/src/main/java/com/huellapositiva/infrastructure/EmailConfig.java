package com.huellapositiva.infrastructure;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConditionalOnProperty(name = "huellapositiva.feature.email.enabled", havingValue = "true")
public class EmailConfig {

    @Autowired
    private AwsSesProperties awsSesProperties;

    @Value("${huellapositiva.feature.email.enabled}")
    private Boolean servusw;

    @Bean
    @Profile({"dev", "prod"})
    public AmazonSimpleEmailService getAwsSesClient() {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(awsSesProperties.getRegion())
                .build();
    }

    @Bean
    @Profile("!dev & !prod")
    public AmazonSimpleEmailService getLocalstackAwsSesClient() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsSesProperties.getAccessKey(), awsSesProperties.getSecretKey());
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsSesProperties.getEndpoint(), awsSesProperties.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

}
