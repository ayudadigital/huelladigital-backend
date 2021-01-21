package com.huellapositiva.domain.service;

import com.huellapositiva.infrastructure.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

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
        String destinationFileName = UUID.randomUUID().toString();
        destinationFileName += getExtension(image.getOriginalFilename());
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
        String extension = getExtension(cv.getOriginalFilename());
        String destinationFileName = UUID.randomUUID() + extension;
        String volunteerCVRootKey = "cv/volunteers/" + volunteerId + '/';
        return storageService.upload(volunteerCVRootKey + destinationFileName, cv.getInputStream(), cv.getContentType());
    }

    /**
     * This method reads the bytes from the photo of a proposal and uploads it to the storage service
     *
     * @param photo New photo uploaded to the application
     * @param volunteerId Id volunteer stored in database
     * @return URL with the photo location in the storage
     * @throws IOException Exception occurred while uploading the image to the cloud
     */
    public URL uploadVolunteerPhoto(MultipartFile photo, String volunteerId) throws IOException {
        String extension;
        extension = getExtension(photo.getOriginalFilename());
        String destinationFileName = UUID.randomUUID() + extension;
        String volunteerPhotoRootKey = "photo/volunteers/" + volunteerId + '/';
        String content = photo.getContentType();
        return storageService.upload(volunteerPhotoRootKey + destinationFileName, photo.getInputStream(), photo.getContentType());
    }

    public URL uploadNewsletterExcel(InputStream excel) throws IOException {
        String extension = ".xlsx";
        String destinationFileName = UUID.randomUUID() + extension;
        String volunteerExcelRootKey = "newsletter/";
        return storageService.upload(volunteerExcelRootKey + destinationFileName, excel, "application/vnd.ms-excel");
    }
}
