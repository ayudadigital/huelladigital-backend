package com.huellapositiva.domain.service;

import com.huellapositiva.infrastructure.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {

    @Autowired
    private final StorageService storageService;

    public URL uploadProposalImage(MultipartFile file) throws IOException {
        String imageId = UUID.randomUUID().toString();
        File destinationFile = new File(
                System.getProperty("java.io.tmpdir") + File.separator + imageId
        );
        file.transferTo(destinationFile);
        String proposalImageRootKey = "Images/Proposals/";
        return storageService.upload(destinationFile, proposalImageRootKey + imageId);
    }
}
