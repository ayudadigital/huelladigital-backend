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
public class UploadCurriculumVitaeAction {

    private final RemoteStorageService remoteStorageService;

    private final VolunteerRepository volunteerRepository;

    /**
     * This method uploads a file which contains the volunteer resumé (CV) and links its URL to the volunteer
     *
     * @param cv New curriculum uploaded to the application
     * @param volunteerEmail Email volunteer logged
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile cv, String volunteerEmail) throws IOException {
        if (cv.getSize() > 5200000) {
            throw new InvalidFieldException("The curriculum is too bigger");
        }
        if (cv.getInputStream().available() != 0) {
            Volunteer volunteer = volunteerRepository.findByEmail(volunteerEmail);
            URL cvUrl = remoteStorageService.uploadVolunteerCV(cv, volunteer.getId().toString());
            volunteer.setCurriculumVitae(cvUrl);
            volunteerRepository.updateCurriculumVitae(volunteer);
        } else {
            throw new EmptyFileException("There is not any cv attached or is empty.");
        }
    }
}
