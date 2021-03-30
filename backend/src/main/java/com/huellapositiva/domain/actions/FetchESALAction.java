package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.GetESALResponseDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class FetchESALAction {

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private JpaESALRepository jpaESALRepository;

    public GetESALResponseDto executeAsOwner(String esalId, String contactPersonId) throws IllegalAccessException {
        JpaESAL jpaESAL = jpaESALRepository.findByNaturalId(esalId)
                .orElseThrow(() -> new EntityNotFoundException("ESAL not found. ID: " + esalId));

        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByAccountIdWithProfile(contactPersonId)
                .orElseThrow(() -> new UserNotFoundException("Contact Person not found. Account ID: " + contactPersonId));
        JpaESAL joinedEsal = jpaContactPerson.getJoinedEsal();
        if (joinedEsal == null || !joinedEsal.getId().equals(jpaESAL.getId())) {
            throw new IllegalAccessException("The logged contact person is not the owner of that ESAL.");
        }
        return getESALResponse(jpaESAL).build();
    }

    private GetESALResponseDto.GetESALResponseDtoBuilder getESALResponse(JpaESAL jpaESAL) {
        GetESALResponseDto.GetESALResponseDtoBuilder builder = GetESALResponseDto.builder();
        builder.name(jpaESAL.getName())
                .description(jpaESAL.getDescription())
                .website(jpaESAL.getWebsite())
                .registeredEntity(jpaESAL.isRegisteredEntity())
                .entityType(jpaESAL.getEntityType());


        JpaLocation location = jpaESAL.getLocation();
        builder.island(location.getIsland())
                .zipCode(location.getZipCode())
                .province(location.getProvince())
                .town(location.getTown())
                .address(location.getAddress());

        return builder;
    }
}
