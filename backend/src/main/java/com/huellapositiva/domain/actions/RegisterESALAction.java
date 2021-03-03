package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.domain.exception.UserAlreadyHasESALException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.EntityType;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterESALAction {

    private final ESALContactPersonRepository esalContactPersonRepository;

    private final ESALRepository esalRepository;

    /**
     * This method creates an ESAL and links it to the logged user.
     *
     * @param dto contains the info to create a new ESAL
     * @param accountId contact person's account ID
     * @throws UserAlreadyHasESALException in case that loggedContactPersonEmail is associated already with an ESAL
     */
    public void execute(ESALRequestDto dto, String accountId) {
        ContactPerson contactPerson = esalContactPersonRepository.findByAccountId(accountId);

        if (contactPerson.hasESAL()) {
            throw new UserAlreadyHasESALException();
        }
        ESAL esal = ESAL.builder()
                .id(Id.newId())
                .name(dto.getName())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .location(Location.builder().zipCode(dto.getZipCode()).island(dto.getIsland()).build())
                .entityType(EntityType.valueOf(dto.getEntityType()))
                .dataProtectionPolicy(dto.isDataProtectionPolicy())
                .privacyPolicy(dto.isPrivacyPolicy())
                .registeredEntity(dto.isRegisteredEntity())
                .contactPersonEmail(contactPerson.getEmailAddress())
                .build();
        esalRepository.save(esal);
    }

    /**
     * This method creates an ESAL, only for revisers
     *
     * @param dto contains the info to create a new ESAL
     */
    public void execute(ESALRequestDto dto) {
        ESAL esal = ESAL.builder()
                .id(Id.newId())
                .name(dto.getName())
                .description(dto.getDescription())
                .website(dto.getWebsite())
                .location(Location.builder().zipCode(dto.getZipCode()).island(dto.getIsland()).build())
                .entityType(EntityType.valueOf(dto.getEntityType()))
                .dataProtectionPolicy(dto.isDataProtectionPolicy())
                .privacyPolicy(dto.isPrivacyPolicy())
                .registeredEntity(dto.isRegisteredEntity())
                .build();
        esalRepository.saveAsReviser(esal);
    }
}
