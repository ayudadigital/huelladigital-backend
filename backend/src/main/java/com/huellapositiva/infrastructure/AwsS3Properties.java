package com.huellapositiva.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix="aws.s3")
public class AwsS3Properties {

    private String endpoint;

    private String region;

    private Buckets buckets;

    @Data
    public static class Buckets {

        private String data;

        private String mgmt;
    }

    public String getDataBucketName() {
        return buckets.data;
    }

    public String getManagementBucketName() {
        return buckets.mgmt;
    }
}
