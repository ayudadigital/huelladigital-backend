package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.service.RemoteStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.huellapositiva.domain.util.FileUtils.getExtension;

@Service
public class UploadPhotoAction {

    private final Set<String> imageExtensions = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif"));

    private final RemoteStorageService remoteStorageService;

    private final VolunteerRepository volunteerRepository;

    private final int profileImageMaxBytes;

    private final int profileImageMaxWidth;

    private final int profileImageMaxHeight;

    public UploadPhotoAction(RemoteStorageService remoteStorageService, VolunteerRepository volunteerRepository,
                             @Value("${huellapositiva.profile.image.max-bytes}") int profileImageMaxBytes,
                             @Value("${huellapositiva.profile.image.max-width}") int profileImageMaxWidth,
                             @Value("${huellapositiva.profile.image.max-height}") int profileImageMaxHeight) {
        this.remoteStorageService = remoteStorageService;
        this.volunteerRepository = volunteerRepository;
        this.profileImageMaxBytes = profileImageMaxBytes;
        this.profileImageMaxWidth = profileImageMaxWidth;
        this.profileImageMaxHeight = profileImageMaxHeight;
    }


    /**
     * Upload and link a profile image to a volunteer.
     *
     * @param photo new photo of profile
     * @param volunteerEmail email of user
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile photo, String volunteerEmail) throws IOException {
        validateProfileImage(photo);

        Volunteer volunteer = volunteerRepository.findByEmail(volunteerEmail);
        URL photoUrl = remoteStorageService.uploadVolunteerPhoto(photo, volunteer.getId().toString());
        volunteer.setPhoto(photoUrl);
        volunteerRepository.updatePhoto(volunteer);
    }

    /**
     * Validate profile image.
     *
     * @param photo new photo of profile
     */
    private void validateProfileImage(MultipartFile photo) throws IOException {
        if (photo.getSize() > profileImageMaxBytes) {
            throw new InvalidFieldException("The profile image size is too big. Max size: " + profileImageMaxBytes);
        }
        String extension = getExtension(photo.getOriginalFilename());
        if(!imageExtensions.contains(extension.toLowerCase())) {
            throw new FileTypeNotSupportedException("Profile image must have one of the following extensions: " + imageExtensions);
        }
        InputStream is = photo.getInputStream();
        if(is.available() == 0){
            throw new EmptyFileException("The profile image must not be empty.");
        }

        BufferedImage image = ImageIO.read(is);
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > profileImageMaxWidth || height > profileImageMaxHeight) {
            throw new InvalidFieldException("The profile image resolution is too big. Max dimensions: " + profileImageMaxWidth + "x" + profileImageMaxHeight);
        }
    }
}
