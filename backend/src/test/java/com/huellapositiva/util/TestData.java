package com.huellapositiva.util;

import com.huellapositiva.domain.repository.CredentialRepository;
import com.huellapositiva.domain.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class TestData {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    public void resetData() {
        volunteerRepository.deleteAll();
        credentialRepository.deleteAll();
    }
}
