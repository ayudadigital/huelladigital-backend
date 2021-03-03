package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ImageService;
import com.huellapositiva.domain.service.RemoteStorageService;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.AccessDeniedException;

@Service
@AllArgsConstructor
public class UpdateProposalImageAction {

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private EmailCommunicationService emailCommunicationService;

    @Autowired
    private JpaContactPersonRepository contactPersonRepository;

    @Autowired
    private ImageService imageService;

    private final RemoteStorageService remoteStorageService;

    public void execute(MultipartFile photo, String accountId, String proposalId) throws IOException {
        imageService.validateProfileImage(photo);
        JpaContactPerson accountContactPerson = jpaContactPersonRepository.findByAccountId(accountId).orElseThrow(EntityNotFoundException::new);
        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        validateProposalUpdate(accountContactPerson,jpaProposal);

        URL url = remoteStorageService.uploadProposalImage(photo, proposalId);
        Proposal proposal = Proposal.parseJpa(jpaProposal);
        proposal.setImage(url);
        proposal.setStatus(ProposalStatus.REVIEW_PENDING);
        proposalRepository.save(proposal);

        emailCommunicationService.sendProposalImageUpdateEmail(EmailAddress.from(accountContactPerson.getCredential().getEmail()));
    }

    private void validateProposalUpdate(JpaContactPerson accountContactPerson, JpaProposal jpaProposal) throws AccessDeniedException {
        JpaContactPerson proposalContactPerson = contactPersonRepository.findByEsalId(jpaProposal.getEsal().getId()).orElseThrow(EntityNotFoundException::new);
        if(!accountContactPerson.getCredential().getEmail().equals(proposalContactPerson.getCredential().getEmail())){
            throw new AccessDeniedException("The contact person related to this proposal does not match the logged contact person.");
        }
        if(!jpaProposal.getStatus().getId().equals(ProposalStatus.PUBLISHED.getId()) &&
                !jpaProposal.getStatus().getId().equals(ProposalStatus.REVIEW_PENDING.getId())){
            throw new IllegalStateException();
        }
    }
}
