package com.huellapositiva.domain.repository;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.infrastructure.orm.model.Location;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.Proposal;
import com.huellapositiva.infrastructure.orm.repository.JpaLocationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Transactional
@AllArgsConstructor
public class ProposalRepository {

    @Autowired
    private final JpaOrganizationRepository jpaOrganizationRepository;

    @Autowired
    private final JpaLocationRepository jpaLocationRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    public Integer save(ProposalRequestDto dto) {
        Organization organization = jpaOrganizationRepository.findByName(dto.getOrganizationName())
                .orElseThrow(() -> new RuntimeException("Organization " + dto.getOrganizationName() + " not found"));
        Location proposalLocation = jpaLocationRepository.save(Location.builder()
                .province(dto.getProvince())
                .town(dto.getTown())
                .address(dto.getAddress())
                .build());
        Date expirationDate;
        try {
            expirationDate = new SimpleDateFormat("dd-MM-yyyy").parse(dto.getExpirationDate());
        } catch(ParseException ex){
            throw new RuntimeException("Could not format the following date: " + dto.getExpirationDate(), ex);
        }
        Proposal proposal = Proposal.builder()
                .title(dto.getTitle())
                .organization(organization)
                .location(proposalLocation)
                .expirationDate(expirationDate)
                .requiredDays(dto.getRequiredDays())
                .minimumAge(dto.getMinimumAge())
                .maximumAge(dto.getMaximumAge())
                .published(dto.isPublished())
                .build();
        return jpaProposalRepository.save(proposal).getId();
    }
}
