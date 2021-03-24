package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.GetESALResponseDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FetchESALAction {

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    public GetESALResponseDto execute(String accountId) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByAccountIdWithProfile(accountId)
                .orElseThrow(() -> new UserNotFoundException("Contact Person not found. Account ID: " + accountId));
        JpaESAL joinedEsal = jpaContactPerson.getJoinedEsal();

        GetESALResponseDto.GetESALResponseDtoBuilder builder = GetESALResponseDto.builder();
        builder.name(joinedEsal.getName())
                .description(joinedEsal.getDescription())
                .website(joinedEsal.getWebsite())
                .registeredEntity(joinedEsal.isRegisteredEntity())
                .entityType(joinedEsal.getEntityType());


        JpaLocation location = joinedEsal.getLocation();
        builder.island(location.getIsland())
                .zipCode(location.getZipCode())
                .province(location.getProvince())
                .town(location.getTown())
                .address(location.getAddress());

        return builder.build();
    }
}
