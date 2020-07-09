package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ProposalService {

    @Autowired
    private final ProposalRepository proposalRepository;

    public Integer registerProposal(ProposalRequestDto proposalRequestDto) {
        try {
             return proposalRepository.save(proposalRequestDto);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist the proposal due to a conflict.", ex);
            throw new FailedToPersistProposal("Conflict encountered while storing the proposal in database. Constraints were violated.", ex);
        }
    }
}
