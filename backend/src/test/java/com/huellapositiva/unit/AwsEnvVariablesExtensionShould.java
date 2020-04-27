package com.huellapositiva.unit;

import com.amazonaws.SDKGlobalConfiguration;
import com.huellapositiva.util.AwsEnvVariablesExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(AwsEnvVariablesExtension.class)
class AwsEnvVariablesExtensionShould {

    @Test
    void sets_environment_variables() {
        assertThat(System.getenv(SDKGlobalConfiguration.ACCESS_KEY_ENV_VAR), is("access_key"));
        assertThat(System.getenv(SDKGlobalConfiguration.SECRET_KEY_ENV_VAR), is("secret_key"));
        assertThat(System.getenv(SDKGlobalConfiguration.AWS_REGION_ENV_VAR), is("eu-west-1"));
        assertThat(System.getenv("AWS_SES_ENDPOINT_HOST"), is("http://localhost:4579"));
    }
}
