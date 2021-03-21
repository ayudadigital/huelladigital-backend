package com.huellapositiva.domain.service;

import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.huellapositiva.domain.util.FileUtils.getExtension;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {

    @Autowired
    private final StorageService storageService;

    /**
     * This method reads the bytes from the image of a proposal and uploads it to the storage service
     *
     * @param image New image uploaded
     * @param proposalId Id proposal in database
     * @return URL with the image location in the storage
     * @throws IOException Exception occurred while uploading the image to the cloud
     */
    public URL uploadProposalImage(MultipartFile image, String proposalId) throws IOException {
        String destinationFileName = Id.newId() + getExtension(image.getOriginalFilename());
        String proposalImageRootKey = "images/proposals/" + proposalId + '/';
        return storageService.upload(proposalImageRootKey + destinationFileName, image.getInputStream(), image.getContentType());
    }

    /**
     * This method reads the bytes from the CV of a proposal and uploads it to the storage service
     *
     * @param cv New curriculum uploaded
     * @param volunteerId Id volunteer stored in the database
     * @return URL with the cv location in the storage
     * @throws IOException Exception occurred while uploading the image to the cloud
     */
    public URL uploadVolunteerCV(MultipartFile cv, String volunteerId) throws IOException {
        String destinationFileName = Id.newId() + getExtension(cv.getOriginalFilename());
        String volunteerCVRootKey = "cv/volunteers/" + volunteerId + '/';
        return storageService.upload(volunteerCVRootKey + destinationFileName, cv.getInputStream(), cv.getContentType());
    }

    /**
     * This method reads the bytes from the photo of a volunteer and uploads it to the storage service
     *
     * @param photo New photo uploaded to the application
     * @param volunteerId Id of the volunteer stored in database
     * @return URL with the photo location in the storage
     * @throws IOException Exception occurred while uploading the image to the cloud
     */
    public URL uploadVolunteerPhoto(MultipartFile photo, String volunteerId) throws IOException {
        String destinationFileName = Id.newId() + getExtension(photo.getOriginalFilename());
        String volunteerPhotoRootKey = "photo/volunteers/" + volunteerId + '/';
        return storageService.upload(volunteerPhotoRootKey + destinationFileName, photo.getInputStream(), photo.getContentType());
    }

    /**
     * This method reads the bytes from the photo of a contact person and uploads it to the storage service
     *
     * @param photo New photo uploaded to the application
     * @param contactPersonId Id of the contact person stored in database
     * @return URL with the photo location in the storage
     * @throws IOException Exception occurred while uploading the image to the cloud
     */
    public URL uploadContactPersonPhoto(MultipartFile photo, String contactPersonId) throws IOException {
        String destinationFileName = Id.newId() + getExtension(photo.getOriginalFilename());
        String contactPersonPhotoRootKey = "photo/contactPerson/" + contactPersonId + '/';
        return storageService.upload(contactPersonPhotoRootKey + destinationFileName, photo.getInputStream(), photo.getContentType());
    }

    /**
     * This method reads the bytes from the excel and uploads it to the storage service
     *
     * @param excel InputStream uploaded to the application
     * @return URL with the excel location in the storage
     */
    public URL uploadNewsletterExcel(InputStream excel) {
        String extension = ".xlsx";
        String destinationFileName = Id.newId() + extension;
        String volunteerExcelRootKey = "newsletter/";
        return storageService.upload(volunteerExcelRootKey + destinationFileName, excel, "application/vnd.ms-excel");
    }

    /**
     * This method reads the bytes from the logo of an ESAL and uploads it to the storage service
     *
     * @param logo New logo uploaded to the application
     * @param esalId Id of the ESAL stored in database
     * @return URL with the logo location in the storage
     * @throws IOException Exception occurred while uploading the image to the cloud
     */
    public URL uploadESALLogo(MultipartFile logo, String esalId) throws IOException {
        String destinationFileName = Id.newId() + getExtension(logo.getOriginalFilename());
        String volunteerPhotoRootKey = "logo/esal/" + esalId + '/';
        return storageService.upload(volunteerPhotoRootKey + destinationFileName, logo.getInputStream(), logo.getContentType());
    }

}
