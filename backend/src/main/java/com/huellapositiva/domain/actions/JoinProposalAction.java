package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.infrastructure.orm.entities.Volunteer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JoinProposalAction {

    private final ProposalService proposalService;

    private final VolunteerService volunteerService;

    public void execute(Integer proposalId, String volunteerEmail){
        Volunteer volunteer = volunteerService.findVolunteerByEmail(volunteerEmail);
        proposalService.enrollVolunteer(proposalId, volunteer);
    }
}
