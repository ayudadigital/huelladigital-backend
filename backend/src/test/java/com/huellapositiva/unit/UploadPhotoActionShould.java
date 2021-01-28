package com.huellapositiva.unit;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.actions.UploadPhotoAction;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.service.RemoteStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UploadPhotoActionShould {

    @Mock
    private RemoteStorageService remoteStorageService;

    @Mock
    private VolunteerRepository volunteerRepository;

    private UploadPhotoAction uploadPhotoAction;

    @BeforeEach
    void beforeEach() {
        uploadPhotoAction = new UploadPhotoAction(remoteStorageService, volunteerRepository, 1024000, 400, 400);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectPhotos")
    void return_exception_if_do_not_valid_image(String image) throws IOException {
        String name = "photo.png";
        String contentType = "image/png";
        byte[] content = getClass().getClassLoader().getResourceAsStream("images/" + image).readAllBytes();

        MultipartFile result = new MockMultipartFile(name, image, contentType, content);

        assertThrows(InvalidFieldException.class, () -> uploadPhotoAction.execute(result, DEFAULT_EMAIL));
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

        assertThrows(FileTypeNotSupportedException.class, () -> uploadPhotoAction.execute(result, DEFAULT_EMAIL));
    }
}
