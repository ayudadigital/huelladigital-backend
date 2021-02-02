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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.huellapositiva.domain.util.FileUtils.getExtension;

@Service
public class UploadCurriculumVitaeAction {

    private final Set<String> imageExtensions = new HashSet<>(Arrays.asList(".pdf", ".doc", ".docx", ".odt"));

    private final RemoteStorageService remoteStorageService;

    private final VolunteerRepository volunteerRepository;

    private final int cvMaxBytes;

    public UploadCurriculumVitaeAction(RemoteStorageService remoteStorageService, VolunteerRepository volunteerRepository,
                             @Value("${huellapositiva.profile.cv.max-bytes}") int cvMaxBytes) {
        this.remoteStorageService = remoteStorageService;
        this.volunteerRepository = volunteerRepository;
        this.cvMaxBytes = cvMaxBytes;
    }

    /**
     * Upload a file which contains the volunteer resumé (CV) and links its URL to the volunteer
     *
     * @param cv New curriculum uploaded to the application
     * @param accountId Account ID of the volunteer
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile cv, String accountId) throws IOException {
        validateCvFile(cv);

        Volunteer volunteer = volunteerRepository.findByAccountId(accountId);
        URL cvUrl = remoteStorageService.uploadVolunteerCV(cv, volunteer.getId().toString());
        volunteer.setCurriculumVitae(cvUrl);
        volunteerRepository.updateCurriculumVitae(volunteer);
    }

    /**
     * Validate CV file.
     *
     * @param cv CV file
     */
    private void validateCvFile(MultipartFile cv) throws IOException {
        if (cv.getSize() > cvMaxBytes) {
            throw new InvalidFieldException("The CV file size is too big. Max size: " + cvMaxBytes);
        }

        String extension = getExtension(cv.getOriginalFilename());
        if(!imageExtensions.contains(extension.toLowerCase())) {
            throw new FileTypeNotSupportedException("CV file must have one of the following extensions: " + imageExtensions);
        }

        InputStream is = cv.getInputStream();
        if(is.available() == 0){
            throw new EmptyFileException("The CV must not be empty.");
        }
    }
}
