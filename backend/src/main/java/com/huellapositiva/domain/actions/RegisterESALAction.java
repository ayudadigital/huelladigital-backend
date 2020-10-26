package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.domain.exception.UserAlreadyHasESALException;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.domain.service.ESALContactPersonService;
import com.huellapositiva.domain.service.ESALService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterESALAction {

    private final ESALService esalService;

    private final ESALContactPersonService esalContactPersonService;

    private final ESALRepository esalRepository;

    /**
     * This method creates an ESAL and links it to the logged user.
     *
     * @param dto contains the info to create a new ESAL
     * @param loggedContactPersonEmail
     * @throws UserAlreadyHasESALException in case that loggedContactPersonEmail is associated already with an ESAL
     */
    public void execute(ESALRequestDto dto, EmailAddress loggedContactPersonEmail) {
        if (esalService.isUserAssociatedWithAnESAL(loggedContactPersonEmail)) {
            throw new UserAlreadyHasESALException();
        }
        Id id = esalRepository.newId();
        ESAL esal = new ESAL(dto.getName(), id, loggedContactPersonEmail);
        esalRepository.save(esal);
    }

    /**
     * This method creates an ESAL, only for revisers
     *
     * @param dto contains the info to create a new ESAL
     */
    public void execute(ESALRequestDto dto) {
        ESAL esal = new ESAL(dto.getName(), esalRepository.newId());
        esalRepository.saveAsReviser(esal);
    }
}
