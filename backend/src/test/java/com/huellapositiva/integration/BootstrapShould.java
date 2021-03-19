package com.huellapositiva.integration;

import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeParametersResult;
import com.huellapositiva.util.AwsEnvVariablesExtension;
import com.huellapositiva.util.DockerCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test-ssm")
@ExtendWith(DockerCondition.class)
@ExtendWith(AwsEnvVariablesExtension.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { "ssm" }, imageTag = "0.11.5")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class BootstrapShould {

    @Autowired
    private AWSSimpleSystemsManagement ssmClient;

    @Test
    void query_parameter_store() {
        DescribeParametersRequest describeParametersRequest = new DescribeParametersRequest();

        DescribeParametersResult describeParametersResult = ssmClient.describeParameters(describeParametersRequest);

        assertThat(describeParametersResult.getSdkHttpMetadata().getHttpStatusCode()).isEqualTo(200);
    }
}
