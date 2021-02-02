package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.OperationNotAllowedException;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteESALAction {

    private final ESALRepository esalRepository;

    private final ESALContactPersonRepository esalContactPersonRepository;

    /**
     * This method fetches the ESAL associated to the contact person email and deletes it from DB.
     *
     * @param accountId email associated to the ESAL to be deleted.
     * @param requesterId ESAL id given from a request path variable.
     * @throws OperationNotAllowedException if the requesterId and the id from the DB are not the same.
     */
    public void execute(String accountId, String requesterId){
        String esalId = esalContactPersonRepository.getJoinedESAL(accountId).getId().toString();
        if(!requesterId.equals(esalId)) {
            throw new OperationNotAllowedException("The given ESAL ID does not match the user's ESAL ID.");
        }
        esalRepository.delete(esalId);
    }
}
