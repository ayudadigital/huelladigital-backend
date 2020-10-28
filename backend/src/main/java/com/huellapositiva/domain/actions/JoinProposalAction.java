package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JoinProposalAction {

    private final ProposalService proposalService;

    private final VolunteerRepository volunteerRepository;

    /**
     * This method fetches a Volunteer from the DB based of his email and enrolls it to the given proposal
     *
     * @param proposalId id of the proposal to be enrolled by the volunteer
     * @param volunteerEmail
     */
    public void execute(String proposalId, String volunteerEmail){
        Volunteer volunteer = volunteerRepository.findByEmail(volunteerEmail);
        proposalService.enrollVolunteer(proposalId, volunteer);
    }
}
