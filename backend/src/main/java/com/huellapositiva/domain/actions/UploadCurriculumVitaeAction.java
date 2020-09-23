package com.huellapositiva.domain.actions;

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
     * @param cv
     * @param volunteerEmail
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile cv, String volunteerEmail) throws IOException {
        Volunteer volunteer = volunteerRepository.findByEmail(volunteerEmail);
        URL cvUrl = remoteStorageService.uploadVolunteerCV(cv, volunteer.getId().toString());
        volunteer.setCurriculumVitae(cvUrl);
        volunteerRepository.updateCurriculumVitae(volunteer);
    }
}
