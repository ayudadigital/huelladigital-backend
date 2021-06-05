package com.huellapositiva;

import com.huellapositiva.domain.service.ReviserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class App implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Autowired
    private ReviserService reviserService;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("===== SSM properties: {} -> {}", env.getActiveProfiles(), datasourceUrl);
        reviserService.createDefaultReviser();
    }
}
