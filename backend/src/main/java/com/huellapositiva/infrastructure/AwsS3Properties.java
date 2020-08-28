package com.huellapositiva.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix="aws.s3")
class AwsS3Properties {

    private String accessKey;

    private String secretKey;

    private String endpoint;

    private String region;

    private String bucketName;
}
