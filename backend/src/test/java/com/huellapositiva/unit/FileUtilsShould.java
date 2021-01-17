package com.huellapositiva.unit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.huellapositiva.domain.util.FileUtils.getExtension;
import static org.assertj.core.api.Assertions.assertThat;

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
}
