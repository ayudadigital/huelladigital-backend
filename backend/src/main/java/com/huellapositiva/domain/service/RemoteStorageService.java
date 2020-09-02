package com.huellapositiva.domain.service;

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

    public URL uploadProposalImage(MultipartFile file, String proposalId) throws IOException {
        String destinationFileName = UUID.randomUUID() + "." + getExtension(file.getOriginalFilename());
        String proposalImageRootKey = "images/proposals/" + proposalId + '/';
        return storageService.upload(proposalImageRootKey + destinationFileName, file.getInputStream(), file.getContentType());
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index != -1 ? fileName.substring(index) : "";
    }
}
