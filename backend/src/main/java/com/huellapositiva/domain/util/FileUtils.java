package com.huellapositiva.domain.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Slf4j
public class FileUtils {

    private FileUtils() {

    }

    /**
     * Get the extension of the fileName
     *
     * @param fileName Name of file to upload to extract its extension
     * @return the extension or an empty string when there is no extension
     */
    public static String getExtension(String fileName) {
        if (fileName != null) {
            int index = fileName.lastIndexOf('.');
            return index != -1  && fileName.substring(index).trim().length() > 1 ? fileName.substring(index) : "";
        }
        return "";
    }

    public static String getResourceContent(String relativePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(relativePath).getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Failed to open resource file {}", relativePath, e);
            throw e;
        }
    }
}
