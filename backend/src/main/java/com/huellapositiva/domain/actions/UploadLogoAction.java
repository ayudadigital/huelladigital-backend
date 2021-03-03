package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.service.ImageService;
import com.huellapositiva.domain.service.RemoteStorageService;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class UploadLogoAction {

    @Autowired
    private final RemoteStorageService remoteStorageService;

    @Autowired
    private final ESALContactPersonRepository esalContactPersonRepository;

    @Autowired
    private final JpaESALRepository jpaESALRepository;

    @Autowired
    private final ImageService imageService;
    /**
     * Upload and link a profile image to a volunteer.
     *
     * @param logo new logo of esal
     * @param accountId Account ID of contact_person
     * @throws IOException when the cv is corrupted
     */
    public void execute(MultipartFile logo, String accountId) throws IOException {
        imageService.validateEsalLogo(logo);

        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(accountId);
        URL logoURL = remoteStorageService.uploadESALLogo(logo, joinedESAL.getId().toString());
        jpaESALRepository.updateLogo(joinedESAL.getId().getValue(), logoURL.toExternalForm());
    }
}
