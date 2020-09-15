package com.huellapositiva.infrastructure;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bootstrap.BootstrapConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile({"dev", "prod"})
@BootstrapConfiguration
public class BootstrapConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public AWSSimpleSystemsManagement awsSimpleSystemsManagement() {
        log.info("Setting up parameter-store in {} region ...", region);
        return AWSSimpleSystemsManagementClientBuilder.standard()
                .withCredentials(InstanceProfileCredentialsProvider.getInstance())
                .withRegion(region)
                .build();
    }
}
