package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
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
    @Transactional
    @Query("UPDATE JpaVolunteer p SET p.profile = :profile WHERE p.id = :id")
    Integer updateProfile(@Param("id") String id, @Param("profile") JpaProfile profile);
}
