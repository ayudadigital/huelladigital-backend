package com.huellapositiva.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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
    public URL upload(String key, InputStream inputStream, String contentType) {
        String bucketName = awsS3Properties.getDataBucketName();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        PutObjectRequest request = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead);
        awsS3Client.putObject(request);
        return awsS3Client.getUrl(bucketName, key);
    }
}