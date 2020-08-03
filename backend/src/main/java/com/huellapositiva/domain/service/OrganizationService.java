package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.application.exception.OrganizationAlreadyExists;
import com.huellapositiva.domain.ExpressRegistrationOrganization;
import com.huellapositiva.domain.repository.OrganizationRepository;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizationService {

    @Autowired
    private final OrganizationRepository organizationRepository;

    @Autowired
    private final JpaOrganizationRepository jpaOrganizationRepository;

    public Integer create(OrganizationRequestDto dto) {
        jpaOrganizationRepository.findByName(dto.getName()).ifPresent(s -> {throw new OrganizationAlreadyExists();});
        try {
            return organizationRepository.save(new ExpressRegistrationOrganization(dto.getName()));
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist the proposal due to a conflict.", ex);
            throw new FailedToPersistProposal("Conflict encountered while storing the proposal in database. Constraints were violated.", ex);
        }
    }

    public Organization findById(Integer id) {
        return organizationRepository.findById(id);
    }
}
