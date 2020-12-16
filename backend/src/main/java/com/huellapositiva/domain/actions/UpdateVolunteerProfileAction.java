package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.ProfileCredentials;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.model.valueobjects.ProfileLocation;
import com.huellapositiva.domain.model.valueobjects.ProfileVolunteer;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaLocationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class UpdateVolunteerProfileAction {

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private JpaLocationRepository jpaLocationRepository;

    public void execute(ProfileDto profileDto, String email) throws IOException {
        if (someFieldIsEmptyCredentials(profileDto)) {
            throw new IOException("Some field is null");
        }

        JpaLocation jpaLocation = null;
        boolean isLocationNotEmpty = profileDto.getAddress() != null ||
                profileDto.getTown() != null ||
                profileDto.getProvince() != null ||
                profileDto.getZipCode() != null;
        if(isLocationNotEmpty) {
            jpaLocation = updateLocation(profileDto, email);
        }

        /////////////////////////

        /*ProfileVolunteer profileVolunteer = ProfileVolunteer.builder().twitter(profileDto.getTwitter())
                .instagram(profileDto.getInstagram())
                .linkedin(profileDto.getLinkedin())
                .additionalInformation(profileDto.getAdditionalInformation()).build();*/

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(email);
        //updateVolunteerInformation(profileVolunteer, jpaVolunteer);
        //jpaVolunteerRepository.save(jpaVolunteer);

        // Esto merece ser función
        jpaVolunteer.setTwitter(profileDto.getTwitter());
        jpaVolunteer.setInstagram(profileDto.getInstagram());
        jpaVolunteer.setLinkedin(profileDto.getLinkedin());
        jpaVolunteer.setAdditionalInformation(profileDto.getAdditionalInformation());
        // Hasta aqui la función, y que devuelva jpaVolunteer

        ////////////////

        //Do this code at the end
        /*ProfileCredentials profileCredentials =  ProfileCredentials.builder().name(profileDto.getName())
                .surname(profileDto.getSurname())
                .birthDate(ProfileCredentials.parseToLocalDate(profileDto.getBirthDate()))
                .email(profileDto.getEmail())
                .phoneNumber(profileDto.getPhoneNumber()).build();*/

        // Esto merece ser función
        jpaVolunteer.getCredential().setName(profileDto.getName());
        jpaVolunteer.getCredential().setSurname(profileDto.getSurname());
        jpaVolunteer.getCredential().setEmail(profileDto.getEmail());
        jpaVolunteer.getCredential().setBirthDate(parseToLocalDate(profileDto.getBirthDate()));
        jpaVolunteer.getCredential().setPhoneNumber(profileDto.getPhoneNumber());
        // Hasta aqui la función, y que devuelva jpaVolunteer

        /*jpaCredentialRepository.updateProfile(email,
                profileCredentials.getName(),
                profileCredentials.getSurname(),
                profileCredentials.getEmail(),
                profileCredentials.getPhoneNumber(),
                profileCredentials.getBirthDate());*/

        //Send email after commit profile changes

        jpaVolunteer.setLocation(jpaLocation);
        jpaVolunteerRepository.save(jpaVolunteer);

    }

    private void updateVolunteerInformation(ProfileVolunteer profileVolunteer, JpaVolunteer jpaVolunteer) {
        jpaVolunteer.setTwitter(profileVolunteer.getTwitter());
        jpaVolunteer.setInstagram(profileVolunteer.getInstagram());
        jpaVolunteer.setLinkedin(profileVolunteer.getLinkedin());
        jpaVolunteer.setAdditionalInformation(profileVolunteer.getAdditionalInformation());
    }

    // Aqui hay que poner otro IF más, por si el usuario decidiese NO actualizar la información de location cuando
    // no hay location en base de datos todavía.
    private JpaLocation updateLocation(ProfileDto profileDto, String email) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(email);
        String id;
        if (jpaVolunteer.getLocation() == null) {
            id = Id.newId().toString();
        } else {
            id = jpaVolunteer.getLocation().getId();
        }

        JpaLocation jpaLocation = JpaLocation.builder()
                .id(id)
                .province(profileDto.getProvince())
                .town(profileDto.getTown())
                .address(profileDto.getAddress())
                .zipCode(profileDto.getZipCode()).build();
        //jpaVolunteer.setLocation(jpaLocation);
        //jpaVolunteerRepository.save(jpaVolunteer);
        return jpaLocation;
    }

    private boolean someFieldIsEmptyCredentials(ProfileDto profile) {
        if (profile.getName() == null
                || profile.getSurname() == null
                || profile.getBirthDate() == null
                || profile.getEmail() == null
                || profile.getPhoneNumber() == null) {
            return true;
        } else {
            return false;
        }
    }

    public LocalDate parseToLocalDate(String date){
        String[] parts = date.split("-");
        return LocalDate.of(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
    }
}
