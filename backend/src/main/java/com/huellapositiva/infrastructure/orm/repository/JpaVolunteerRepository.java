package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface JpaVolunteerRepository extends JpaRepository<JpaVolunteer, Integer> {

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH c.roles WHERE v.id = :id")
    Optional<JpaVolunteer> findByIdWithCredentialsAndRoles(@Param("id") String id);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c WHERE v.credential.email = :email")
    Optional<JpaVolunteer> findByEmail(@Param("email") String email);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH v.location d WHERE v.credential.email = :email")
    JpaVolunteer findByEmailProfileInformation(@Param("email") String email);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c WHERE v.id = :id")
    Optional<JpaVolunteer> findById(@Param("id") String id);

    @Modifying
    @Query("UPDATE JpaVolunteer v SET v.curriculumVitaeUrl = :cvUrl WHERE v.id = :id")
    Integer updateCurriculumVitae(@Param("id") String id, @Param("cvUrl") String cvUrl);

    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteer v SET v.twitter = :twitter, v.instagram = :instagram, v.linkedin = :linkedin, v.additionalInformation = :additionalInformation WHERE v.id = :id")
    Integer updateProfile(@Param("id") String id,
                          @Param("twitter") String twitter,
                          @Param("instagram") String instagram,
                          @Param("linkedin") String linkedin,
                          @Param("additionalInformation") String additionalInformation);

    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteer v SET v.location = :location WHERE v.id = :id")
    Integer updateIdLocation(@Param("id") String id, @Param("location") JpaLocation location);

    @Modifying
    @Query("UPDATE JpaVolunteer v SET v.photoUrl = :photoUrl WHERE v.id = :id")
    Integer updatePhoto(@Param("id") String id, @Param("photoUrl") String photoUrl);
}
