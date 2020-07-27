package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.service.OrganizationMemberService;
import com.huellapositiva.domain.service.OrganizationService;
import com.huellapositiva.infrastructure.orm.model.OrganizationMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationAction {

    private final OrganizationService organizationService;

    private final OrganizationMemberService organizationMemberService;

    public void execute(OrganizationRequestDto dto, String memberEmail) {
        OrganizationMember member = organizationMemberService.findByEmail(memberEmail)
                .orElseThrow(() -> new UserNotFound("Could not retrieve the organization member by his email."));

        if(member.getJoinedOrganization() != null){
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED);
        }

        Integer organizationId = organizationService.create(dto);
        organizationService.findById(organizationId);
        organizationMemberService.updateJoinedOrganization(member, organizationService.findById(organizationId));
    }

    public void execute(OrganizationRequestDto dto) {
        organizationService.create(dto);
    }
}
