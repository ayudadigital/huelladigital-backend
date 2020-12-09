package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ChangeStatusVolunteerDto;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteersProposalsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeStatusVolunteerAction {

    @Autowired
    private JpaVolunteersProposalsRepository jpaVolunteersProposalsRepository;

    /**
     * This method change the status of volunteers in the proposal. The statuses are rejected (false) and confirmed (true).
     *
     * @param changeStatusVolunteerDtos List<ChangeStatusVolunteerDto> This param has the information to change the
     *                                  status of volunteers with the ID proposal and ID volunteer.
     */
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
    }

}
