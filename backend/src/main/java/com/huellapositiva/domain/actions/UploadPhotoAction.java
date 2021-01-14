package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.exception.FileTypeNotSupportedException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.service.RemoteStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class UploadPhotoAction {

    private final RemoteStorageService remoteStorageService;

    private final VolunteerRepository volunteerRepository;

    private final Set<String> imageExtensions =
            new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif"));

    /**
     * This method uploads a file which contains the volunteer resumé (CV) and links its URL to the volunteer
     *
     * @param photo new photo of profile
     * @param volunteerEmail email of user
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile photo, String volunteerEmail) throws IOException {
        if (photo.getSize() > 1100000) {
            throw new InvalidFieldException("The photo is too bigger");
        }
        String extension = getExtension(photo.getOriginalFilename());
        if(!imageExtensions.contains(extension.toLowerCase())) {
            throw new FileTypeNotSupportedException("photo file must be .jpg,.png,.jpeg,.gif");
        }
        BufferedImage image = ImageIO.read(photo.getInputStream());
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > 400 || height > 400) {
            throw new InvalidFieldException("The photo is too bigger");
        }
        if (photo.getInputStream().available() != 0) {
            Volunteer volunteer = volunteerRepository.findByEmail(volunteerEmail);
            URL photoUrl = remoteStorageService.uploadVolunteerPhoto(photo, volunteer.getId().toString());
            volunteer.setPhoto(photoUrl);
            volunteerRepository.updatePhoto(volunteer);
        } else {
            throw new EmptyFileException("There is not any photo attached or is empty.");
        }
    }

    /**
     * This method extracts the extension of the fileName
     *
     * @param fileName Name of file to upload to stract its extension
     * @return the extension or an empty string when there is no extension
     */
    private String getExtension(String fileName) {
        if (fileName != null) {
            int index = fileName.lastIndexOf('.');
            return index != -1 ? fileName.substring(index) : "";
        }
        return "";
    }
}
