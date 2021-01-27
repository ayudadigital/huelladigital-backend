package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.GetProfileResponseDto;
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
     * @param volunteerEmail Email of the logged volunteer
     */
    public GetProfileResponseDto execute(String volunteerEmail) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(volunteerEmail);
        GetProfileResponseDto.GetProfileResponseDtoBuilder profileDto = GetProfileResponseDto.builder().email(jpaVolunteer.getCredential().getEmail());

        if (jpaVolunteer.getProfile() != null) {
            profileDto.name(jpaVolunteer.getProfile().getName())
                    .surname(jpaVolunteer.getProfile().getSurname())
                    .birthDate(jpaVolunteer.getProfile().getBirthDate().toString())
                    .phoneNumber("" + jpaVolunteer.getProfile().getPhoneNumber())
                    .photo(jpaVolunteer.getProfile().getPhotoUrl())
                    .curriculumVitae(jpaVolunteer.getProfile().getCurriculumVitaeUrl())
                    .twitter(jpaVolunteer.getProfile().getTwitter())
                    .instagram(jpaVolunteer.getProfile().getInstagram())
                    .linkedin(jpaVolunteer.getProfile().getLinkedin())
                    .additionalInformation(jpaVolunteer.getProfile().getAdditionalInformation());
        }

        if (jpaVolunteer.getLocation() != null) {
            profileDto.province(jpaVolunteer.getLocation().getProvince())
                    .town(jpaVolunteer.getLocation().getTown())
                    .address(jpaVolunteer.getLocation().getAddress())
                    .zipCode(jpaVolunteer.getLocation().getZipCode())
                    .island(jpaVolunteer.getLocation().getIsland());
        }

        return profileDto.build();
    }
}
