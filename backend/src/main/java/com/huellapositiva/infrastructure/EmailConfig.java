package com.huellapositiva.infrastructure;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    private String sesEndPoint = "http://localhost:4579";
    private String sesRegion = "eu-west-1";
    @Bean
    public AmazonSimpleEmailService getAwsSesClient(){

        BasicAWSCredentials awsCreds = new BasicAWSCredentials("access_key_id", "secret_key_id");
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sesEndPoint, sesRegion))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        // Replace US_WEST_2 with the AWS Region you're using for
                        // Amazon SES.
                        // .withRegion(Regions.EU_WEST_1)
                .build();
    }

}
