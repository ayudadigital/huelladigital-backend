package com.huellapositiva;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.Charset;

@Slf4j
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        log.debug("Platform Charset: {}", Charset.defaultCharset());
        SpringApplication.run(App.class, args);
    }
}
