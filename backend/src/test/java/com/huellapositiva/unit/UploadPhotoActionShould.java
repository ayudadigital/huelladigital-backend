package com.huellapositiva.unit;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.actions.UploadPhotoAction;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import com.huellapositiva.util.TestData;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class UploadPhotoActionShould {

    @Autowired
    private UploadPhotoAction uploadPhotoAction;

    @ParameterizedTest
    @MethodSource("provideIncorrectPhotos")
    void return_exception_if_do_not_valid_image(String image) throws IOException {
        String name = "photo.png";
        String originalFileName = image;
        String contentType = "image/png";
        byte[] content = getClass().getClassLoader().getResourceAsStream("images/" + image).readAllBytes();

        MultipartFile result = new MockMultipartFile(name, originalFileName, contentType, content);

        assertThrows(InvalidFieldException.class, () -> {
            uploadPhotoAction.execute(result, DEFAULT_EMAIL);
        });
    }

    private static Stream<String> provideIncorrectPhotos() {
        return Stream.of(
                "image-height-oversized.png",
                "image-length-oversized.png",
                "oversized.png"
        );
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectNamePhotos")
    void return_exception_if_do_not_contain_extension(String image) throws IOException {
        String name = "photo.png";
        String originalFileName = image;
        String contentType = "image/png";
        byte[] content = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png").readAllBytes();

        MultipartFile result = new MockMultipartFile(name, originalFileName, contentType, content);

        assertThrows(FileTypeNotSupportedException.class, () -> {
            uploadPhotoAction.execute(result, DEFAULT_EMAIL);
        });
    }

    private static Stream<String> provideIncorrectNamePhotos() {
        return Stream.of(
                "",
                "huellapositiva-logo"
        );
    }
}
