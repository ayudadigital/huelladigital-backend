package com.huellapositiva.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.Executors;

@Slf4j
public class DockerCondition implements ExecutionCondition {

    @SneakyThrows
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Process process = Runtime.getRuntime().exec("docker ps");
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();

        String message = "docker ps exit code: " + exitCode;
        log.info(message);
        return exitCode == 0 ? ConditionEvaluationResult.enabled(message) : ConditionEvaluationResult.disabled(message);
    }
}
