package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.repository.OrganizationMemberRepository;
import com.huellapositiva.domain.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteOrganizationAction {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMemberRepository organizationMemberRepository;

    public void execute(String memberEmail){
        Integer id = organizationMemberRepository.getJoinedOrganization(memberEmail).getId();
        organizationRepository.delete(id);
    }
}
