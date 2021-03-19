package com.huellapositiva.util;

import com.amazonaws.SDKGlobalConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.extension.*;

@Slf4j
public class AwsEnvVariablesExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Override
    public void beforeAll(ExtensionContext context) {
        log.debug("Executing custom before all");
        environmentVariables.set(SDKGlobalConfiguration.ACCESS_KEY_ENV_VAR, "dummyaccess");
        environmentVariables.set(SDKGlobalConfiguration.SECRET_KEY_ENV_VAR, "dummysecret");
        environmentVariables.set(SDKGlobalConfiguration.AWS_REGION_ENV_VAR, "us-east-1");
        environmentVariables.set("AWS_SES_ENDPOINT_HOST", "http://localhost:4579");
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        log.debug("Executing custom before each");
    }

    @Override
    public void afterEach(ExtensionContext context) {
        log.debug("Executing custom after each");
    }

    @Override
    public void afterAll(ExtensionContext context) {
        log.debug("Executing custom after all");
    }
}
