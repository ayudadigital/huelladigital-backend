package com.huellapositiva.domain.repository;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.application.exception.ESALNotFound;
import com.huellapositiva.infrastructure.orm.entities.Location;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.repository.JpaLocationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class ProposalRepository {

    @Autowired
    private final JpaLocationRepository jpaLocationRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    @Autowired
    private final JpaESALRepository jpaESALRepository;

    @Value("${huellapositiva.proposal.expiration-hour}")
    private String expirationHour;

    public String save(ProposalRequestDto dto) {
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
        JpaESAL esal = jpaESALRepository.findByName(dto.getEsalName())
                .orElseThrow(ESALNotFound::new);

        JpaProposal proposal = JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title(dto.getTitle())
                .esal(esal)
                .location(proposalLocation)
                .expirationDate(expirationDate)
                .requiredDays(dto.getRequiredDays())
                .minimumAge(dto.getMinimumAge())
                .maximumAge(dto.getMaximumAge())
                .published(dto.isPublished())
                .build();
        return jpaProposalRepository.save(proposal).getId();
    }

    public String save(com.huellapositiva.domain.model.entities.Proposal proposal) {
        Location location = jpaLocationRepository.save(Location.builder()
                .province(proposal.getProvince())
                .town(proposal.getTown())
                .address(proposal.getAddress())
                .build());
        Date expirationDate;
        try {
            expirationDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(proposal.getExpirationDate() + " " + expirationHour);
        } catch(ParseException ex){
            throw new FailedToPersistProposal("Could not format the following date: " + proposal.getExpirationDate(), ex);
        }
        JpaESAL esal = jpaESALRepository.findByName(proposal.getEsal().getName())
                .orElseThrow(ESALNotFound::new);
        JpaProposal jpaProposal = JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title(proposal.getTitle())
                .esal(esal)
                .location(location)
                .expirationDate(expirationDate)
                .requiredDays(proposal.getRequiredDays())
                .minimumAge(proposal.getMinimumAge())
                .maximumAge(proposal.getMaximumAge())
                .published(proposal.isPublished())
                .build();
        return jpaProposalRepository.save(jpaProposal).getId();
    }

    public JpaProposal save(JpaProposal proposal) {
        return jpaProposalRepository.save(proposal);
    }

    public JpaProposal fetch(String id) {
        return jpaProposalRepository.findByNaturalId(id).orElseThrow(EntityNotFoundException::new);
    }
}
