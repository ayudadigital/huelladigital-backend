package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.RemoteStorageService;
import lombok.RequiredArgsConstructor;
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

    private final ProposalRepository proposalRepository;

    private final RemoteStorageService storageService;

    /**
     * This method registers a proposal linked to the ContactPerson's ESAL.
     * Validates the proposals data (such as its dates) and uploads its image before persisting the proposal.
     *
     * @param dto proposal info
     * @param image
     * @param contactPersonEmail
     * @return proposal id
     * @throws ParseException when dto date format is invalid
     * @throws IOException when multipart file is corrupt
     */
    public String execute(ProposalRequestDto dto,
                          MultipartFile image,
                          String contactPersonEmail) throws ParseException, IOException {
        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(contactPersonEmail);
        Proposal proposal = Proposal.parseDto(dto, joinedESAL);
        proposal.validate();
        URL imageUrl = storageService.uploadProposalImage(image, proposal.getId().getValue());
        proposal.setImage(imageUrl);
        return proposalRepository.save(proposal);
    }

    /**
     * This method registers a proposal linked to the given ESAL through the dto. (only for revisers)
     * Validates the proposals data (such as its dates) and uploads its image before persisting the proposal.
     *
     * @param dto proposal info (contains the ESAL name)
     * @param image
     * @return proposal id
     * @throws ParseException when dto date format is invalid
     * @throws IOException when multipart file is corrupt
     */
    public String execute(ProposalRequestDto dto, MultipartFile image) throws ParseException, IOException {
        ESAL esal = esalRepository.findByName(dto.getEsalName());
        Proposal proposal = Proposal.parseDto(dto, esal);
        proposal.validate();
        URL imageUrl = storageService.uploadProposalImage(image, proposal.getId().getValue());
        proposal.setImage(imageUrl);
        return proposalRepository.save(proposal);
    }
}
