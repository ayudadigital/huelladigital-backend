package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
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

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.REVIEW_PENDING;

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
     * @param image image recently upload
     * @param accountId contact person's account ID
     * @return proposal id
     * @throws ParseException when dto date format is invalid
     * @throws IOException when multipart file is corrupt
     */
    public String executeByContactPerson(ProposalRequestDto dto,
                                         MultipartFile image,
                                         String accountId) throws ParseException, IOException {
        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(accountId);
        return saveProposal(dto, image, joinedESAL, REVIEW_PENDING);
    }

    /**
     * This method registers a proposal linked to the given ESAL through the dto. (only for revisers)
     * Validates the proposals data (such as its dates) and uploads its image before persisting the proposal.
     *
     * @param dto proposal info (contains the ESAL name)
     * @param image The image of esal
     * @return proposal id
     * @throws ParseException when dto date format is invalid
     * @throws IOException when multipart file is corrupt
     */
    public String executeByReviser(ProposalRequestDto dto, MultipartFile image) throws ParseException, IOException {
        ESAL esal = esalRepository.findByName(dto.getEsalName());
        return saveProposal(dto, image, esal, PUBLISHED);
    }

    /**
     * This method saves a proposal setting its status depending on who created it.
     * @param dto proposal info from user
     * @param image image for the proposal
     * @param esal esal related to the proposal
     * @param proposalStatus status of the proposal
     * @return id of the proposal
     * @throws ParseException
     * @throws IOException
     */
    private String saveProposal(ProposalRequestDto dto, MultipartFile image, ESAL esal, ProposalStatus proposalStatus) throws ParseException, IOException {
        Proposal proposal = Proposal.parseDto(dto, esal);
        proposal.setStatus(proposalStatus);
        proposal.validate();
        if (image != null) {
            URL imageUrl = storageService.uploadProposalImage(image, proposal.getId().getValue());
            proposal.setImage(imageUrl);
        }
        return proposalRepository.insert(proposal);
    }
}
