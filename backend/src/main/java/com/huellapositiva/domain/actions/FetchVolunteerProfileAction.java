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
    private JpaVolunteerRepository jpaVolunteerRespository;

    public ProfileDto execute(String volunteerEmail) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRespository.findByEmailProfileInformation(volunteerEmail);

        boolean jpaLocationIsNull = jpaVolunteer.getLocation() == null;
        if (jpaLocationIsNull) {
            return ProfileDto.builder()
                    .name(jpaVolunteer.getCredential().getName())
                    .surname(jpaVolunteer.getCredential().getSurname())
                    .birthDate(jpaVolunteer.getCredential().getBirthDate() == null ?
                        null : jpaVolunteer.getCredential().getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .phoneNumber(jpaVolunteer.getCredential().getPhoneNumber())
                    .email(jpaVolunteer.getCredential().getEmail())
                    .province(null)
                    .town(null)
                    .zipCode(null)
                    .address(null)
                    .photo(jpaVolunteer.getPhotoUrl())
                    .curriculumVitae(jpaVolunteer.getCurriculumVitaeUrl())
                    .twitter(jpaVolunteer.getTwitter())
                    .instagram(jpaVolunteer.getInstagram())
                    .linkedin(jpaVolunteer.getLinkedin())
                    .additionalInformation(jpaVolunteer.getAdditionalInformation())
                    .build();
        } else {
            return ProfileDto.builder()
                    .name(jpaVolunteer.getCredential().getName())
                    .surname(jpaVolunteer.getCredential().getSurname())
                    .birthDate(jpaVolunteer.getCredential().getBirthDate() == null ?
                            null : jpaVolunteer.getCredential().getBirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .phoneNumber(jpaVolunteer.getCredential().getPhoneNumber())
                    .email(jpaVolunteer.getCredential().getEmail())
                    .province(jpaVolunteer.getLocation().getProvince())
                    .town(jpaVolunteer.getLocation().getTown())
                    .zipCode(jpaVolunteer.getLocation().getZipCode())
                    .address(jpaVolunteer.getLocation().getAddress())
                    .photo(jpaVolunteer.getPhotoUrl())
                    .curriculumVitae(jpaVolunteer.getCurriculumVitaeUrl())
                    .twitter(jpaVolunteer.getTwitter())
                    .instagram(jpaVolunteer.getInstagram())
                    .linkedin(jpaVolunteer.getLinkedin())
                    .additionalInformation(jpaVolunteer.getAdditionalInformation())
                    .build();
        }

    }
}
