package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.RemoteStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final ESALRepository esalRepository;

    private final ESALContactPersonRepository esalContactPersonRepository;

    @Autowired
    private final ProposalRepository proposalRepository;

    @Autowired
    private final RemoteStorageService storageService;

    public String execute(ProposalRequestDto dto,
                          MultipartFile file,
                          String contactPersonEmail) throws ParseException, IOException {
        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(contactPersonEmail);
        Proposal proposal = Proposal.parseDto(dto, joinedESAL);
        proposal.validate();
        URL imageUrl = storageService.uploadProposalImage(file, proposal.getId().toString());
        proposal.setImage(imageUrl);
        return proposalRepository.save(proposal);
    }

    public String execute(ProposalRequestDto dto) throws ParseException {
        ESAL joinedESAL = esalRepository.findByName(dto.getEsalName());
        Proposal proposal = Proposal.parseDto(dto, joinedESAL);
        proposal.validate();
        return proposalRepository.save(proposal);
    }
}
