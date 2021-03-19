package com.huellapositiva.unit;

import com.huellapositiva.domain.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.huellapositiva.domain.util.FileUtils.getExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUtilsShould {

    @ParameterizedTest
    @MethodSource("fileNameProvider")
    void should_get_file_extension(String fileName, String expectedExtension) {
        assertThat(getExtension(fileName)).isEqualTo(expectedExtension);
    }

    private static Stream<Arguments> fileNameProvider() {
        return Stream.of(Arguments.of("", ""),
                Arguments.of(".", ""),
                Arguments.of(".jpeg", ".jpeg"),
                Arguments.of("png", ""),
                Arguments.of("file.png", ".png"),
                Arguments.of(null, "")
        );
    }

    @Test
    void should_get_resource_file_content() throws IOException {
        String expectedContent = "Test\nEmail";

        String content = FileUtils.getResourceContent("templates/emails/testEmail.txt");

        assertThat(content).isEqualTo(expectedContent);
    }

    @Test
    void should_throw_error_if_resource_does_not_exist() {
        assertThrows(IOException.class, () -> FileUtils.getResourceContent("templates/emails/testEmailTemplateDoesNotExist.txt"));
    }
}
