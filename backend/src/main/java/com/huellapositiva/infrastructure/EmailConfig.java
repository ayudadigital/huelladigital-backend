package com.huellapositiva.infrastructure;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EmailConfig {

    @Autowired
    private AwsSesProperties awsSesProperties;

    @Bean
    @Profile({"!localstack"})
    public AmazonSimpleEmailService getAwsSesClient(){
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(awsSesProperties.getRegion())
                .build();
    }

    @Bean
    @Profile("localstack")
    public AmazonSimpleEmailService getLocalstackAwsSesClient(){
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsSesProperties.getAccessKey(), awsSesProperties.getSecretKey());
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsSesProperties.getEndpoint(), awsSesProperties.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }

}
