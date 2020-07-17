package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.infrastructure.orm.model.Volunteer;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Service
public class JoinProposalAction {

    private final ProposalService proposalService;

    private final VolunteerService volunteerService;

    private final JwtService jwtService;

    public void execute(Integer proposalId, HttpServletRequest req) {
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        String volunteerEmail = null;
        try {
            volunteerEmail = jwtService.getUserDetails(authHeader.replace("Bearer ", "")).getFirst();
        } catch (InvalidJwtTokenException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Volunteer volunteer = volunteerService.findVolunteerByEmail(volunteerEmail);
        proposalService.enrollVolunteer(proposalId, volunteer);
    }
}
