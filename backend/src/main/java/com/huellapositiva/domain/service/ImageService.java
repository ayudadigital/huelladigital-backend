package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.huellapositiva.domain.util.FileUtils.getExtension;

@Service
public class ImageService {

    private final Set<String> imageExtensions = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif"));

    private static final int PROFILE_IMAGE_MAX_BYTES = 1100000;

    private static final int PROFILE_IMAGE_MAX_WIDTH = 400;

    private static final int PROFILE_IMAGE_MAX_HEIGHT = 400;

    private static final int ESAL_LOGO_MAX_BYTES = 1100000;

    private static final int ESAL_LOGO_MAX_WIDTH = 400;

    private static final int ESAL_LOGO_MAX_HEIGHT = 400;

    private void validateImage(MultipartFile photo,
                               int imageMaxBytes,
                               int imageMaxWidth,
                               int imageMaxHeight) throws IOException {
        if (photo.getSize() > imageMaxBytes) {
            throw new InvalidFieldException("The image size is too big. Max size: " + imageMaxBytes);
        }
        String extension = getExtension(photo.getOriginalFilename());
        if(!imageExtensions.contains(extension.toLowerCase())) {
            throw new FileTypeNotSupportedException("Image must have one of the following extensions: " + imageExtensions);
        }
        InputStream is = photo.getInputStream();
        if(is.available() == 0){
            throw new EmptyFileException("The image must not be empty.");
        }

        BufferedImage image = ImageIO.read(is);
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > imageMaxWidth || height > imageMaxHeight) {
            throw new InvalidFieldException("The image resolution is too big. Max dimensions: " + imageMaxWidth + "x" + imageMaxHeight);
        }
    }

    /**
     * Validate profile image.
     *
     * @param photo new photo of profile
     */
    public void validateProfileImage(MultipartFile photo) throws IOException {
        validateImage(photo, PROFILE_IMAGE_MAX_BYTES, PROFILE_IMAGE_MAX_WIDTH, PROFILE_IMAGE_MAX_HEIGHT);
    }

    /**
     * Validate esal logo.
     *
     * @param logo new logo of profile
     */
    public void validateEsalLogo(MultipartFile logo) throws IOException {
        validateImage(logo, ESAL_LOGO_MAX_BYTES, ESAL_LOGO_MAX_WIDTH, ESAL_LOGO_MAX_HEIGHT);
    }
}
