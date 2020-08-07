package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.OperationNotAllowed;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteESALAction {

    private final ESALRepository esalRepository;

    private final ESALContactPersonRepository esalContactPersonRepository;

    public void execute(String memberEmail, int requesterId){
        Integer contextId = esalContactPersonRepository.getJoinedESAL(memberEmail).getId().asInt();
        if(requesterId != contextId){
            throw new OperationNotAllowed("The given ESAL ID does not match the user's ESAL ID.");
        }
        esalRepository.delete(contextId);
    }
}
