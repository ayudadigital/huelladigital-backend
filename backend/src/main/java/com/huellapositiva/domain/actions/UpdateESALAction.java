package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateESALDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateESALAction {

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private JpaESALRepository jpaESALRepository;

    public void execute(UpdateESALDto dto, String accountId) {
        JpaContactPerson contactPerson = jpaContactPersonRepository.findByAccountId(accountId)
                .orElseThrow(UserNotFoundException::new);
        JpaESAL esal = contactPerson.getJoinedEsal();
        updateESAL(esal, dto);
        updateLocation(esal, dto);
        jpaESALRepository.save(esal);
    }

    private void updateESAL(JpaESAL esal, UpdateESALDto dto) {
        esal.setName(dto.getName());
        esal.setDescription(dto.getDescription());
        esal.setWebsite(dto.getWebsite());
        esal.setRegisteredEntity(dto.isRegisteredEntity());
        esal.setEntityType(dto.getEntityType());
    }

    private void updateLocation(JpaESAL esal, UpdateESALDto dto) {
        JpaLocation esalLocation = esal.getLocation();
        esalLocation.setIsland(dto.getIsland());
        esalLocation.setZipCode(dto.getZipCode());
        esalLocation.setProvince(dto.getProvince());
        esalLocation.setTown(dto.getTown());
        esalLocation.setAddress(dto.getAddress());
        esal.setLocation(esalLocation);
    }
}
