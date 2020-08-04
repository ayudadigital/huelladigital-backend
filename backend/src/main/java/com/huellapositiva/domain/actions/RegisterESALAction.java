package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.exception.ESALAlreadyExists;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.domain.service.ESALMemberService;
import com.huellapositiva.domain.service.ESALService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterESALAction {

    private final ESALService ESALService;

    private final ESALMemberService ESALMemberService;

    private final ESALRepository ESALRepository;

    public void execute(ESALRequestDto dto, EmailAddress memberEmail) {
        ESAL ESAL = new ESAL(dto.getName());
        ContactPerson contactPerson = ESALMemberService.fetch(memberEmail);
        ESAL.addContactPerson(contactPerson);
        try {
            ESALRepository.save(ESAL);
        } catch (DataIntegrityViolationException ex) {
            throw new ESALAlreadyExists();
        }
    }

    public void execute(ESALRequestDto dto) {
        ESALService.create(dto);
    }
}
