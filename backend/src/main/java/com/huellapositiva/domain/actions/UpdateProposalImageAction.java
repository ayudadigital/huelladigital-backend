package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.RemoteStorageService;
import com.huellapositiva.infrastructure.EmailService;
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

@Service
@AllArgsConstructor
public class UpdateProposalImageAction {

    @Autowired
    JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    JpaProposalRepository jpaProposalRepository;

    @Autowired
    ProposalRepository proposalRepository;

    @Autowired
    EmailCommunicationService emailCommunicationService;

    private final RemoteStorageService remoteStorageService;

    public void execute(MultipartFile photo, String accountId, String proposalId) throws IOException {
        //Validar la photo con el Image Service
        JpaContactPerson contactPerson = jpaContactPersonRepository.findByAccountId(accountId).orElseThrow(EntityNotFoundException::new);
        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        /*
        if(!contactPerson.getCredential().getEmail().equals(proposal.getEsal().getContactPersonEmail)){
            //throw new Exception
        }
         */
        if(!jpaProposal.getStatus().getId().equals(ProposalStatus.PUBLISHED.getId()) &&
                jpaProposal.getStatus().getId().equals(ProposalStatus.REVIEW_PENDING.getId())){
            //throw new Exception
        }

        URL url = remoteStorageService.uploadProposalImage(photo, proposalId);
        Proposal proposal = Proposal.parseJpa(jpaProposal);
        proposal.setImage(url);
        proposal.setStatus(ProposalStatus.REVIEW_PENDING);
        proposalRepository.save(proposal);

        emailCommunicationService.sendProposalImageUpdateEmail(EmailAddress.from(contactPerson.getCredential().getEmail()));
    }
}
