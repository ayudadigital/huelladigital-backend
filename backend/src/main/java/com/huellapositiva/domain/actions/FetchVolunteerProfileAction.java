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
        ProfileDto.ProfileDtoBuilder profileDto = ProfileDto.builder().email(jpaVolunteer.getCredential().getEmail());
        addProfileInProfileDto(jpaVolunteer, profileDto);
        addLocationInProfileDto(jpaVolunteer, profileDto);
        return profileDto.build();
    }

    private void addLocationInProfileDto(JpaVolunteer jpaVolunteer, ProfileDto.ProfileDtoBuilder profileDto) {
        boolean jpaLocationIsNotNull = jpaVolunteer.getLocation() != null;
        if (jpaLocationIsNotNull) {
            profileDto.province(jpaVolunteer.getLocation().getProvince())
                    .town(jpaVolunteer.getLocation().getTown())
                    .address(jpaVolunteer.getLocation().getAddress())
                    .zipCode(jpaVolunteer.getLocation().getZipCode())
                    .island(jpaVolunteer.getLocation().getIsland());
        }
    }

    private void addProfileInProfileDto(JpaVolunteer jpaVolunteer, ProfileDto.ProfileDtoBuilder profileDto) {
        boolean jpaProfileIsNotNull = jpaVolunteer.getProfile() != null;
        if (jpaProfileIsNotNull) {
            profileDto.name(jpaVolunteer.getProfile().getName())
                    .surname(jpaVolunteer.getProfile().getSurname())
                    .birthDate(jpaVolunteer.getProfile().getBirthDate() == null ?
                            null : jpaVolunteer.getProfile().getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .phoneNumber("" + jpaVolunteer.getProfile().getPhoneNumber())
                    .photo(jpaVolunteer.getProfile().getPhotoUrl())
                    .curriculumVitae(jpaVolunteer.getProfile().getCurriculumVitaeUrl())
                    .twitter(jpaVolunteer.getProfile().getTwitter())
                    .instagram(jpaVolunteer.getProfile().getInstagram())
                    .linkedin(jpaVolunteer.getProfile().getLinkedin())
                    .additionalInformation(jpaVolunteer.getProfile().getAdditionalInformation());
        }
    }
}
