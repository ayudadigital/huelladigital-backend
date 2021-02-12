package com.huellapositiva.unit;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import com.huellapositiva.domain.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageServiceShould {

    private ImageService imageService;

    @BeforeEach
    void beforeEach() {
        imageService = new ImageService();
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectPhotos")
    void return_exception_if_do_not_valid_image(String image) throws IOException {
        String name = "photo.png";
        String contentType = "image/png";
        byte[] content = getClass().getClassLoader().getResourceAsStream("images/" + image).readAllBytes();

        MultipartFile result = new MockMultipartFile(name, image, contentType, content);

        assertThrows(InvalidFieldException.class, () -> imageService.validateProfileImage(result));
        assertThrows(InvalidFieldException.class, () -> imageService.validateEsalLogo(result));
    }

    private static Stream<String> provideIncorrectPhotos() {
        return Stream.of(
                "image-height-oversized.png",
                "image-length-oversized.png",
                "oversized.png"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "huellapositiva-logo"})
    void return_exception_if_do_not_contain_extension(String image) throws IOException {
        String name = "photo.png";
        String contentType = "image/png";
        byte[] content = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png").readAllBytes();

        MultipartFile result = new MockMultipartFile(name, image, contentType, content);

        assertThrows(FileTypeNotSupportedException.class, () -> imageService.validateProfileImage(result));
        assertThrows(FileTypeNotSupportedException.class, () -> imageService.validateEsalLogo(result));
    }
}
