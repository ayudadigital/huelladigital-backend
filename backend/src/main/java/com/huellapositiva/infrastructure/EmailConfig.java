package com.huellapositiva.infrastructure;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Bean
    public AmazonSimpleEmailService getAwsSesClient(){
        return AmazonSimpleEmailServiceClientBuilder.standard()
                        // Replace US_WEST_2 with the AWS Region you're using for
                        // Amazon SES.
                        .withRegion(Regions.EU_WEST_1).build();
    }

}
