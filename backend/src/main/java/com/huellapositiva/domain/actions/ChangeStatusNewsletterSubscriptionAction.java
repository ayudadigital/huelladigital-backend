package com.huellapositiva.domain.actions;


import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChangeStatusNewsletterSubscriptionAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    public void execute(Boolean newsletter, String email){
        JpaVolunteer volunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(email);
        volunteer.getProfile().setNewsletter(newsletter);
        jpaVolunteerRepository.save(volunteer);
    }
}