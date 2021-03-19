package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.GetProfileResponseDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FetchVolunteerProfileAction {

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    /**
     * This method fetches a Volunteer with full information (with partially credentials and location) from the DB
     *
     * @param accountId Account ID of the logged volunteer
     */
    public GetProfileResponseDto execute(String accountId) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByAccountIdWithCredentialAndLocationAndProfile(accountId)
                .orElseThrow(() -> new UserNotFoundException("Volunteer not found. Account ID: " + accountId));
        GetProfileResponseDto.GetProfileResponseDtoBuilder profileDto = GetProfileResponseDto.builder().email(jpaVolunteer.getCredential().getEmail());

        JpaProfile jpaProfile = jpaVolunteer.getProfile();
        if (jpaProfile != null) {
            profileDto.name(jpaProfile.getName())
                    .surname(jpaProfile.getSurname())
                    .phoneNumber(jpaProfile.getPhoneNumber())
                    .photo(jpaProfile.getPhotoUrl())
                    .curriculumVitae(jpaProfile.getCurriculumVitaeUrl())
                    .twitter(jpaProfile.getTwitter())
                    .instagram(jpaProfile.getInstagram())
                    .linkedin(jpaProfile.getLinkedin())
                    .additionalInformation(jpaProfile.getAdditionalInformation());
            if (jpaProfile.getBirthDate() != null) {
                profileDto.birthDate(jpaProfile.getBirthDate().toString());
            }
        }

        JpaLocation jpaLocation = jpaVolunteer.getLocation();
        if (jpaLocation != null) {
            profileDto.province(jpaLocation.getProvince())
                    .town(jpaLocation.getTown())
                    .address(jpaLocation.getAddress())
                    .zipCode(jpaLocation.getZipCode())
                    .island(jpaLocation.getIsland());
        }

        return profileDto.build();
    }
}
