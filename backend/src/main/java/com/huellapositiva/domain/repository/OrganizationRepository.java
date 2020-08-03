package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationOrganization;
import com.huellapositiva.infrastructure.orm.entities.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationMemberRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
public class OrganizationRepository {

    @Autowired
    private final JpaOrganizationRepository jpaOrganizationRepository;

    @Autowired
    private final JpaOrganizationMemberRepository jpaOrganizationMemberRepository;

    public Integer save(ExpressRegistrationOrganization expressOrganization) {
        Organization organization = Organization.builder()
                .name(expressOrganization.getName())
                .build();
        return jpaOrganizationRepository.save(organization).getId();
    }

    public Organization findById(Integer id) {
        return jpaOrganizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find the organization by the provided ID"));
    }

    public Integer save(com.huellapositiva.domain.model.entities.Organization model) {
        Organization organization = Organization.builder()
                .name(model.getName())
                .build();
        Integer id = jpaOrganizationRepository.save(organization).getId();
        jpaOrganizationMemberRepository.updateJoinedOrganization(model.getMember().getId().asInt(), organization);
        return id;
    }

    public void delete(int id) {
        jpaOrganizationMemberRepository.unlinkMembersOfOrganization(id);
        jpaOrganizationRepository.deleteById(id);
    }
}
