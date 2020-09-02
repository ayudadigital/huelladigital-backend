package com.huellapositiva.integration;

import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.huellapositiva.infrastructure.AwsStorageService;
import com.huellapositiva.util.AwsEnvVariablesExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(AwsEnvVariablesExtension.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { "s3" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"huellapositiva.feature.storage.enabled=true"})
class AwsRemoteStorageServiceShould {

    @Autowired
    private AwsStorageService awsStorageService;

    private File tmpFile;

    @AfterEach
    void delete_tmpFile() {
        tmpFile.delete();
    }

    @Test
    void not_throw_any_exception_uploading_an_image() throws IOException {
        tmpFile = new File(System.currentTimeMillis() + ".txt");
        tmpFile.createNewFile();
        String key = "Images/Proposals/" + tmpFile.getName();

        assertThatCode(() -> awsStorageService.upload(tmpFile, key)).doesNotThrowAnyException();
    }

    @Test
    void return_a_working_url_after_uploading_an_image() throws IOException {
        tmpFile = new File(System.currentTimeMillis() + ".txt");
        tmpFile.createNewFile();
        String key = "Images/Proposals/" + tmpFile.getName();
        URL imageUrl = awsStorageService.upload(tmpFile, key);
        get(imageUrl).then().assertThat().statusCode(HttpStatus.OK.value());
    }
}
