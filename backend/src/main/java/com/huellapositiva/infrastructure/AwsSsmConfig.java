package com.huellapositiva.infrastructure;

import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Slf4j
@BootstrapConfiguration
public class AwsSsmConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${aws.ssm.endpoint}")
    private String ssmUrl;

    @Bean
    @Profile({"dev", "prod"})
    public AWSSimpleSystemsManagement awsSimpleSystemsManagement() {
        log.info("Setting up parameter-store in {} region ...", region);
        return AWSSimpleSystemsManagementClientBuilder.standard()
                .withCredentials(new EC2ContainerCredentialsProviderWrapper())
                .withRegion(region)
                .build();
    }

    @Bean
    @Profile("dev-alt")
    public AWSSimpleSystemsManagement devAlternateSimpleSystemsManagement() {
        log.info("Setting up lazy parameter-store in {} region ...", region);
        return AWSSimpleSystemsManagementClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    @Bean
    @Profile("test-ssm")
    public AWSSimpleSystemsManagement localAwsSimpleSystemsManagement() {
        log.info("Setting up local parameter-store {} in {} region ...", ssmUrl, region);
        return AWSSimpleSystemsManagementClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(ssmUrl, region))
                .build();
    }
}
