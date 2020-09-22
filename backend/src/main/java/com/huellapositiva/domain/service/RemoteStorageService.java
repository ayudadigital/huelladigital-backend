package com.huellapositiva.domain.service;

import com.huellapositiva.domain.exception.FileTypeNotSupported;
import com.huellapositiva.infrastructure.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {

    @Autowired
    private final StorageService storageService;

    /**
     * This method reads the bytes from the image of a proposal and uploads it to the storage service
     *
     * @param image
     * @param proposalId
     * @return URL with the image location in the storage
     * @throws IOException
     */
    public URL uploadProposalImage(MultipartFile image, String proposalId) throws IOException {
        String destinationFileName = UUID.randomUUID() + getExtension(image.getOriginalFilename());
        String proposalImageRootKey = "images/proposals/" + proposalId + '/';
        return storageService.upload(proposalImageRootKey + destinationFileName, image.getInputStream(), image.getContentType());
    }

    /**
     * This method reads the bytes from the CV of a proposal and uploads it to the storage service
     *
     * @param cv
     * @param volunteerId
     * @return URL with the cv location in the storage
     * @throws IOException
     */
    public URL uploadVolunteerCV(MultipartFile cv, String volunteerId) throws IOException {
        String extension = getExtension(cv.getOriginalFilename());
        if(!extension.equalsIgnoreCase(".pdf")) {
            throw new FileTypeNotSupported("Curriculum vitae file must be .pdf");
        }
        String destinationFileName = UUID.randomUUID() + extension;
        String volunteerCVRootKey = "cv/volunteers/" + volunteerId + '/';
        return storageService.upload(volunteerCVRootKey + destinationFileName, cv.getInputStream(), cv.getContentType());
    }

    /**
     * This method extracts the extension of the fileName
     *
     * @param fileName
     * @return the extension or an empty string when there is no extension
     */
    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index != -1 ? fileName.substring(index) : "";
    }
}
