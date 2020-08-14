package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final ESALRepository esalRepository;

    private final ESALContactPersonRepository esalContactPersonRepository;

    @Autowired
    private final ProposalRepository proposalRepository;

    public String execute(ProposalRequestDto dto, String contactPersonEmail) throws ParseException {
        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(contactPersonEmail);
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title(dto.getTitle())
                .esal(joinedESAL)
                .expirationDate(new SimpleDateFormat("dd-MM-yyyy").parse(dto.getExpirationDate()))
                .maximumAge(dto.getMaximumAge())
                .minimumAge(dto.getMinimumAge())
                .location(new Location(dto.getProvince(), dto.getTown(), dto.getAddress()))
                .requiredDays(dto.getRequiredDays())
                .published(dto.isPublished())
                .build();
        return proposalRepository.save(proposal);
    }

    public String execute(ProposalRequestDto dto) throws ParseException {
        ESAL joinedESAL = esalRepository.findByName(dto.getEsalName());
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title(dto.getTitle())
                .esal(joinedESAL)
                .expirationDate(new SimpleDateFormat("dd-MM-yyyy").parse(dto.getExpirationDate()))
                .maximumAge(dto.getMaximumAge())
                .minimumAge(dto.getMinimumAge())
                .location(new Location(dto.getProvince(), dto.getTown(), dto.getAddress()))
                .requiredDays(dto.getRequiredDays())
                .published(dto.isPublished())
                .build();
        return proposalRepository.save(proposal);
    }
}
