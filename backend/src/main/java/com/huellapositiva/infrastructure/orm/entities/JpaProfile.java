package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "profile")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaProfile implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer surrogateKey;

        @NaturalId
        @Column(name = "id")
        private String id;

        @Column(name = "name")
        private String name;

        @Column(name = "surname")
        private String surname;

        @Column(name = "phone_number")
        private String phoneNumber;

        @Column(name = "birth_date")
        private LocalDate birthDate;

        @Column(name = "curriculum_vitae_url")
        private String curriculumVitaeUrl;

        @Column(name = "photo_url")
        private String photoUrl;

        @Column(name = "twitter")
        private String twitter;

        @Column(name = "instagram")
        private String instagram;

        @Column(name = "linkedin")
        private String linkedin;

        @Column(name = "additional_information")
        private String additionalInformation;

        @Column(name = "newsletter")
        private boolean newsletter;
}
