package com.huellapositiva.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;

@Slf4j
@Service
@ConditionalOnProperty(name = "huellapositiva.feature.storage.enabled", havingValue = "true")
public class AwsStorageService implements StorageService {

    @Autowired
    private AmazonS3 awsS3Client;

    @Autowired
    private AwsS3Properties awsS3Properties;

    @Override
    public URL upload(File file, String key) {
        String bucketName = awsS3Properties.getBucketName();
        awsS3Client.putObject(
                bucketName,
                key,
                file
        );
        return awsS3Client.getUrl(bucketName, key);
    }
}