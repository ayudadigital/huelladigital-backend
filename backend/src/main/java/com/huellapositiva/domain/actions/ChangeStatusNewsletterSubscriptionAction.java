package com.huellapositiva.domain.actions;


import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChangeStatusNewsletterSubscriptionAction {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    public void execute(Boolean subscribed, String email){
        if(subscribed){
            //jpaVolunteerRepository.updateToSubscribed(email);
        }else if(subscribed == false){
            //jpaVolunteerRepository.updateToNotSubscribed(email);
        }
    }
}
