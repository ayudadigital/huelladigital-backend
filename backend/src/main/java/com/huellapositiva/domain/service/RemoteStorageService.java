package com.huellapositiva.domain.service;

import com.huellapositiva.infrastructure.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {

    @Autowired
    private final StorageService storageService;

    public URL uploadProposalImage(MultipartFile file, String proposalId) throws IOException {
        String destinationFileName = proposalId + "." + file.getContentType();
        File destinationFile = new File(
                System.getProperty("java.io.tmpdir") + File.separator + destinationFileName
        );
        file.transferTo(destinationFile);
        String proposalImageRootKey = "Images/Proposals/";
        return storageService.upload(destinationFile, proposalImageRootKey + destinationFileName);
    }
}
