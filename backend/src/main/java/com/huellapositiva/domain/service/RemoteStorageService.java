package com.huellapositiva.domain.service;

import com.huellapositiva.domain.exception.FileTypeNotSupported;
import com.huellapositiva.infrastructure.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {

    @Autowired
    private final StorageService storageService;

    public URL uploadProposalImage(MultipartFile file, String proposalId) throws IOException {
        String basename = UUID.randomUUID().toString();
        String destinationFileName = getExtension(file.getOriginalFilename()).map(basename::concat).orElse(basename);
        String proposalImageRootKey = "images/proposals/" + proposalId + '/';
        return storageService.upload(proposalImageRootKey + destinationFileName, file.getInputStream(), file.getContentType());
    }

    public URL uploadVolunteerCV(MultipartFile file, String volunteerId) throws IOException {
        String extension = getExtension(file.getOriginalFilename()).orElse("");
        if(!".pdf".equalsIgnoreCase(extension)) {
            throw new FileTypeNotSupported("Curriculum vitae file must be .pdf");
        }
        String destinationFileName = UUID.randomUUID() + extension;
        String volunteerCVRootKey = "cv/volunteers/" + volunteerId + '/';
        return storageService.upload(volunteerCVRootKey + destinationFileName, file.getInputStream(), file.getContentType());
    }

    private Optional<String> getExtension(String fileName) {
        return Optional.ofNullable(fileName).map(filename -> {
                    int index = fileName.lastIndexOf('.');
                    return index >= 0 ? fileName.substring(index) : null;
                });
    }
}
