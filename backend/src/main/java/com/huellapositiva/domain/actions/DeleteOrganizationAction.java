package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.OperationNotAllowed;
import com.huellapositiva.domain.repository.OrganizationMemberRepository;
import com.huellapositiva.domain.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOrganizationAction {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMemberRepository organizationMemberRepository;

    public void execute(String memberEmail, int requesterId){
        Integer contextId = organizationMemberRepository.getJoinedOrganization(memberEmail).getId();
        if(requesterId != contextId){
            throw new OperationNotAllowed("The given organization ID does not match the user's organization ID.");
        }
        organizationRepository.delete(contextId);
    }
}
