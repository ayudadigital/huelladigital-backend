package com.huellapositiva.infrastructure;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "huellapositiva.feature.storage.enabled", havingValue = "true")
public class AwsS3Config {

    @Autowired
    private AwsS3Properties awsS3Properties;

    @Bean
    @Profile({"dev", "prod"})
    public AmazonS3 getAwsS3Client() {
        log.info("Amazon S3 client enabled");
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withRegion(awsS3Properties.getRegion())
                .build();

        createBucketIfNotExists(s3client, awsS3Properties.getBucketName());

        return s3client;
    }

    @Bean
    @Profile("!dev & !prod")
    public AmazonS3 getLocalstackAwsS3Client() {
        log.info("Localstack Amazon S3 client enabled");
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsS3Properties.getEndpoint(), awsS3Properties.getRegion()))
                .build();

        createBucketIfNotExists(s3client, awsS3Properties.getBucketName());

        return s3client;
    }

    private void createBucketIfNotExists(AmazonS3 s3client, String bucketName) {
        if(!s3client.doesBucketExistV2(bucketName)) {
            CreateBucketRequest request = new CreateBucketRequest(bucketName);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            s3client.createBucket(new CreateBucketRequest(bucketName));
        }
    }
}
