package com.huellapositiva.util;

import com.huellapositiva.infrastructure.orm.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class TestData {

    @Autowired
    private JpaVolunteerRepository volunteerRepository;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    public void resetData() {
        volunteerRepository.deleteAll();
        jpaCredentialRepository.deleteAll();
    }
}
