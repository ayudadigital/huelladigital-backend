package com.huellapositiva.domain.repository;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.infrastructure.orm.model.Location;
import com.huellapositiva.infrastructure.orm.model.Proposal;
import com.huellapositiva.infrastructure.orm.repository.JpaLocationRepository;
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
    private final JpaLocationRepository jpaLocationRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    public Integer save(ProposalRequestDto dto) {
        Location proposalLocation = jpaLocationRepository.save(Location.builder()
                .province(dto.getProvince())
                .town(dto.getTown())
                .address(dto.getAddress())
                .build());
        Date expirationDate;
        try {
            expirationDate = new SimpleDateFormat("dd-MM-yyyy").parse(dto.getExpirationDate());
        } catch(ParseException ex){
            throw new FailedToPersistProposal("Could not format the following date: " + dto.getExpirationDate(), ex);
        }
        Proposal proposal = Proposal.builder()
                .title(dto.getTitle())
                .organization(dto.getOrganization())
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
