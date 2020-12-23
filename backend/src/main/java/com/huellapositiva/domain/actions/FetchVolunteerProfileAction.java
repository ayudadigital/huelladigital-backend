package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class FetchVolunteerProfileAction {

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    /**
     * This method fetches a Volunteer with full information (with partially credentials and location) from the DB
     *
     * @param volunteerEmail Email of the logged volunteer
     */
    public ProfileDto execute(String volunteerEmail) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(volunteerEmail);

        boolean jpaLocationIsNull = jpaVolunteer.getLocation() == null;
        if (jpaLocationIsNull) {
            return getProfileDtoBuilder(jpaVolunteer).build();
        } else {
            return getProfileDtoBuilder(jpaVolunteer)
                    .province(jpaVolunteer.getLocation().getProvince())
                    .town(jpaVolunteer.getLocation().getTown())
                    .address(jpaVolunteer.getLocation().getAddress())
                    .zipCode(jpaVolunteer.getLocation().getZipCode())
                    .island(jpaVolunteer.getLocation().getIsland())
                    .build();
        }
    }

    private ProfileDto.ProfileDtoBuilder getProfileDtoBuilder(JpaVolunteer jpaVolunteer) {
        return ProfileDto.builder()
                .name(jpaVolunteer.getCredential().getName())
                .surname(jpaVolunteer.getCredential().getSurname())
                .birthDate(jpaVolunteer.getCredential().getBirthDate() == null ?
                        null : jpaVolunteer.getCredential().getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .phoneNumber(jpaVolunteer.getCredential().getPhoneNumber())
                .email(jpaVolunteer.getCredential().getEmail())
                .photo(jpaVolunteer.getPhotoUrl())
                .curriculumVitae(jpaVolunteer.getCurriculumVitaeUrl())
                .twitter(jpaVolunteer.getTwitter())
                .instagram(jpaVolunteer.getInstagram())
                .linkedin(jpaVolunteer.getLinkedin())
                .additionalInformation(jpaVolunteer.getAdditionalInformation());
    }
}
