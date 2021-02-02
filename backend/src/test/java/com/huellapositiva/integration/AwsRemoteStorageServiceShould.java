package com.huellapositiva.integration;

import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.huellapositiva.infrastructure.AwsStorageService;
import com.huellapositiva.util.AwsEnvVariablesExtension;
import com.huellapositiva.util.DockerCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static io.restassured.RestAssured.get;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@ExtendWith(DockerCondition.class)
@ExtendWith(AwsEnvVariablesExtension.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { "s3" }, imageTag = "0.11.5")
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"huellapositiva.feature.storage.enabled=true"})
class AwsRemoteStorageServiceShould {

    @Autowired
    private AwsStorageService awsStorageService;

    @Test
    void return_a_working_url_after_uploading_an_image() {
        String key = "images/proposals/" + UUID.randomUUID() + ".png";
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        URL imageUrl = awsStorageService.upload(key, is, IMAGE_PNG_VALUE);

        get(imageUrl).then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .header(CONTENT_LENGTH, "9883")
                .header(CONTENT_TYPE, IMAGE_PNG_VALUE);
    }

    @Test
    void return_a_working_url_after_uploading_a_pdf() {
        String key = "cv/volunteers/" + UUID.randomUUID() + ".pdf";
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        URL documentUrl = awsStorageService.upload(key, is, APPLICATION_PDF_VALUE);

        get(documentUrl).then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .header(CONTENT_LENGTH, "12696")
                .header(CONTENT_TYPE, APPLICATION_PDF_VALUE);
    }
}
