package com.huellapositiva.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {
//
//    @Autowired
//    private final StorageService storageService;
//
//    public URL uploadProposalImage(MultipartFile file, String proposalId) throws IOException {
//        String destinationFileName = proposalId + "." + file.getContentType();
//        File destinationFile = new File(
//                System.getProperty("java.io.tmpdir") + File.separator + destinationFileName
//        );
//        file.transferTo(destinationFile);
//        String proposalImageRootKey = "Images/Proposals/";
//        return storageService.upload(destinationFile, proposalImageRootKey + destinationFileName);
//    }
}
