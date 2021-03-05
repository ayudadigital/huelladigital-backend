package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.repository.ContactPersonRepository;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.service.ImageService;
import com.huellapositiva.domain.service.RemoteStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

@Service
public class UploadPhotoAction {

    private final RemoteStorageService remoteStorageService;

    private final VolunteerRepository volunteerRepository;

    private final ContactPersonRepository contactPersonRepository;

    private final ImageService imageService;

    public UploadPhotoAction(RemoteStorageService remoteStorageService,
                             VolunteerRepository volunteerRepository,
                             ContactPersonRepository contactPersonRepository, ImageService imageService){
        this.remoteStorageService = remoteStorageService;
        this.volunteerRepository = volunteerRepository;
        this.contactPersonRepository = contactPersonRepository;
        this.imageService = imageService;
    }

    /**
     * Upload and link a profile image to a volunteer.
     *
     * @param photo new photo of profile
     * @param accountId Account ID of volunteer
     * @throws IOException when the photo is corrupted
     */
    public void executeAsVolunteer(MultipartFile photo, String accountId) throws IOException {
        imageService.validateProfileImage(photo);

        Volunteer volunteer = volunteerRepository.findByAccountId(accountId);
        URL photoUrl = remoteStorageService.uploadVolunteerPhoto(photo, volunteer.getId().toString());
        volunteer.setPhoto(photoUrl);
        volunteerRepository.updatePhoto(volunteer);
    }

    /**
     * Upload and link a profile image to a contact person.
     *
     * @param photo new photo of profile
     * @param accountId Account ID of contact person
     * @throws IOException when the photo is corrupted
     */
    public void executeAsContactPerson(MultipartFile photo, String accountId) throws IOException {
        imageService.validateProfileImage(photo);

        ContactPerson contactPerson = contactPersonRepository.findByAccountId(accountId);
        URL photoUrl = remoteStorageService.uploadContactPersonPhoto(photo, contactPerson.getId().toString());
        contactPerson.setPhoto(photoUrl);
        contactPersonRepository.updatePhoto(contactPerson);
    }
}
