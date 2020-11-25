package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ChangeStatusVolunteerDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteersProposalsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RejectVolunteerAction {
    private final EmailCommunicationService communicationService;

    @Autowired
    private JpaVolunteersProposalsRepository jpaVolunteersProposalsRepository;

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    public void execute(List<ChangeStatusVolunteerDto> changeStatusVolunteerDtos){
        for(ChangeStatusVolunteerDto volunteerProposal:changeStatusVolunteerDtos){
            if(volunteerProposal.isConfirmed()) {
                jpaVolunteersProposalsRepository.updateVolunteerInProposalConfirmed(
                        volunteerProposal.getIdVolunteer(),
                        volunteerProposal.getIdProposal());
            } else {
                jpaVolunteersProposalsRepository.updateVolunteerInProposalRejected(
                        volunteerProposal.getIdVolunteer(),
                        volunteerProposal.getIdProposal());
            }
        }
        /*
        jpaVolunteersProposalsRepository.updateVolunteerInProposalRejected(idVolunteer, idProposal);

        JpaVolunteer volunteer = jpaVolunteerRepository.findById(idVolunteer).get();
        String volunteerEmail = volunteer.getCredential().getEmail();

        JpaProposal proposal = jpaProposalRepository.findByNaturalId(idProposal).get();
        String proposalTitle = proposal.getTitle();

        EmailAddress emailAddress = EmailAddress.from(volunteerEmail);
        communicationService.sendVolunteerRejectionEmail(emailAddress, proposalTitle);
         */
    }

}
