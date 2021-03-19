package com.huellapositiva.infrastructure;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;

@AllArgsConstructor
@Service
@ConditionalOnProperty(name = "huellapositiva.feature.storage.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpStorageService implements StorageService {

    @Autowired
    private final AwsS3Properties awsS3Properties;

    @SneakyThrows
    @Override
    public URL upload(String key, InputStream inputStream, String contentType) {
        return new URL(awsS3Properties.getEndpoint() + '/' + awsS3Properties.getDataBucketName() + "/no-op/" + key);
    }
}