package com.huellapositiva.domain.repository;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.application.exception.OrganizationNotFound;
import com.huellapositiva.infrastructure.orm.entities.Location;
import com.huellapositiva.infrastructure.orm.entities.Organization;
import com.huellapositiva.infrastructure.orm.entities.Proposal;
import com.huellapositiva.infrastructure.orm.repository.JpaLocationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Transactional
@RequiredArgsConstructor
public class ProposalRepository {

    @Autowired
    private final JpaLocationRepository jpaLocationRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    @Autowired
    private final JpaOrganizationRepository jpaOrganizationRepository;

    @Value("${huellapositiva.proposal.expiration-hour}")
    private String expirationHour;

    public Integer save(ProposalRequestDto dto) {
        Location proposalLocation = jpaLocationRepository.save(Location.builder()
                .province(dto.getProvince())
                .town(dto.getTown())
                .address(dto.getAddress())
                .build());
        Date expirationDate;
        try {
            expirationDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dto.getExpirationDate() + " " + expirationHour);
        } catch(ParseException ex){
            throw new FailedToPersistProposal("Could not format the following date: " + dto.getExpirationDate(), ex);
        }
        Organization organization = jpaOrganizationRepository.findByName(dto.getOrganizationName())
                .orElseThrow(OrganizationNotFound::new);

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

    public Proposal save(Proposal proposal) {
        return jpaProposalRepository.save(proposal);
    }

    public Proposal fetch(Integer id) {
        return jpaProposalRepository.getOne(id);
    }
}
