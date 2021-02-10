package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import org.springframework.beans.factory.annotation.Value;
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

    private final int profileImageMaxBytes;
    private final int profileImageMaxWidth;
    private final int profileImageMaxHeight;

    private final int esalLogoMaxBytes;
    private final int esalLogoMaxWidth;
    private final int esalLogoMaxHeight;

    public ImageService(@Value("${huellapositiva.profile.image.max-bytes}") int profileImageMaxBytes,
                        @Value("${huellapositiva.profile.image.max-width}") int profileImageMaxWidth,
                        @Value("${huellapositiva.profile.image.max-height}") int profileImageMaxHeight,
                        @Value("1100000") int esalLogoMaxBytes,
                        @Value("400") int esalLogoMaxWidth,
                        @Value("400") int esalLogoMaxHeight){
        this.profileImageMaxBytes = profileImageMaxBytes;
        this.profileImageMaxWidth= profileImageMaxWidth;
        this.profileImageMaxHeight = profileImageMaxHeight;
        this.esalLogoMaxBytes = esalLogoMaxBytes;
        this.esalLogoMaxWidth = esalLogoMaxWidth;
        this.esalLogoMaxHeight = esalLogoMaxHeight;
    }

    /**
     * Validate profile image.
     *
     * @param photo new photo of profile
     */
    public void validateProfileImage(MultipartFile photo) throws IOException {
        validateImage(photo, profileImageMaxBytes, profileImageMaxWidth, profileImageMaxHeight);
    }

    private void validateImage(MultipartFile photo, int imageMaxBytes, int imageMaxWidth, int imageMaxHeight) throws IOException {
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
     * Validate esal logo.
     *
     * @param logo new logo of profile
     */
    public void validateEsalLogo(MultipartFile logo) throws IOException {
        validateImage(logo, esalLogoMaxBytes, esalLogoMaxWidth, esalLogoMaxHeight);
    }
}
