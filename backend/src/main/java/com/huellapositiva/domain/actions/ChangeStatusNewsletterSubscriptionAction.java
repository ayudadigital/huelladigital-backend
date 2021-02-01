package com.huellapositiva.domain.actions;


import com.huellapositiva.application.dto.UpdateNewsletterSubscriptionDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.exception.ProfileNotFoundException;
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

    public void execute(UpdateNewsletterSubscriptionDto newsletterSubscriptionDto, String accountId) {
        JpaVolunteer volunteer = jpaVolunteerRepository.findByAccountIdWithCredentialAndLocationAndProfile(accountId)
                .orElseThrow(() -> new UserNotFoundException("Volunteer not found. Account ID: " + accountId));

        if (volunteer.getProfile() == null) {
            throw new ProfileNotFoundException("Profile not found. Account ID: " + accountId);
        }

        volunteer.getProfile().setNewsletter(newsletterSubscriptionDto.isSubscribed());
        jpaVolunteerRepository.save(volunteer);
    }
}