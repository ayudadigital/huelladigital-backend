package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.service.RemoteStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Service
@AllArgsConstructor
public class UploadPhotoAction {

    private final RemoteStorageService remoteStorageService;

    private final VolunteerRepository volunteerRepository;

    /**
     * This method uploads a file which contains the volunteer resumé (CV) and links its URL to the volunteer
     *
     * @param photo new photo of profile
     * @param volunteerEmail email of user
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile photo, String volunteerEmail) throws IOException {
        if (photo.getSize() > 2100) {
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
}
