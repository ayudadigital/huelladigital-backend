package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.OperationNotAllowed;
import com.huellapositiva.domain.repository.ESALMemberRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteESALAction {

    private final ESALRepository ESALRepository;

    private final ESALMemberRepository ESALMemberRepository;

    public void execute(String memberEmail, int requesterId){
        Integer contextId = ESALMemberRepository.getJoinedESAL(memberEmail).getId();
        if(requesterId != contextId){
            throw new OperationNotAllowed("The given ESAL ID does not match the user's ESAL ID.");
        }
        ESALRepository.delete(contextId);
    }
}
