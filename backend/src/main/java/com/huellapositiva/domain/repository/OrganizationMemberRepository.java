package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationOrganizationMember;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationMemberRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static com.huellapositiva.domain.model.valueobjects.Roles.ORGANIZATION_MEMBER_NOT_CONFIRMED;

@Component
@Transactional
@AllArgsConstructor
public class OrganizationMemberRepository {

    @Autowired
    private final JpaOrganizationMemberRepository jpaOrganizationMemberRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    public OrganizationMember findById(Integer id) {
        return jpaOrganizationMemberRepository.findById(id)
                .orElseThrow( () -> new RuntimeException("Organization member not found"));
    }

    public Integer save(ExpressRegistrationOrganizationMember expressMember) {
        Role role = jpaRoleRepository.findByName(ORGANIZATION_MEMBER_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RuntimeException("Role ORGANIZATION_MEMBER_NOT_CONFIRMED not found."));
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(expressMember.getEmail())
                .hash(expressMember.getConfirmationToken())
                .build();
        emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
        Credential credential = Credential.builder()
                .email(expressMember.getEmail())
                .hashedPassword(expressMember.getHashedPassword())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .build();
        OrganizationMember organizationMember = OrganizationMember.builder()
                .credential(credential)
                .build();
        return jpaOrganizationMemberRepository.save(organizationMember).getId();
    }

    public Integer updateOrganization(Integer employeeId, Organization organization) {
        return jpaOrganizationMemberRepository.updateJoinedOrganization(employeeId, organization);
    }

    public Optional<OrganizationMember> findByEmail(String email) {
        return jpaOrganizationMemberRepository.findByEmail(email);
    }

    public Organization getJoinedOrganization(String memberEmail){
        return jpaOrganizationMemberRepository.findByEmail(memberEmail)
                .orElseThrow(UserNotFound::new).getJoinedOrganization();
    }
}
