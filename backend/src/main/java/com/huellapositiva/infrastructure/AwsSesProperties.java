package com.huellapositiva.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix="aws.ses")
class AwsSesProperties {

    private String endpoint;

    private String region;
}
