package com.huellapositiva.infrastructure;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile("test-ssm")
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
                .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                .withRegion(region)
                .build();
    }

    @Bean
    @Profile("test-ssm")
    public AWSSimpleSystemsManagement localAwsSimpleSystemsManagement() {
        log.info("Setting up local parameter-store {} in {} region ...", ssmUrl, region);
        return AWSSimpleSystemsManagementClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4583", region))
                .build();
    }
}
