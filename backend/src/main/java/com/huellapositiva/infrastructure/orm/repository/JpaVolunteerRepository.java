package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaVolunteerRepository extends JpaRepository<JpaVolunteer, Integer> {

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH c.roles WHERE v.id = :id")
    Optional<JpaVolunteer> findByIdWithCredentialsAndRoles(@Param("id") String id);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c WHERE c.id = :accountId")
    Optional<JpaVolunteer> findByAccountIdWithCredentials(@Param("accountId") String accountId);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH v.location d WHERE v.credential.email = :email")
    JpaVolunteer findByEmailWithCredentialAndLocation(@Param("email") String email);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH v.location l LEFT JOIN FETCH v.profile p WHERE v.credential.email = :email")
    JpaVolunteer findByEmailWithCredentialLocationAndProfile(@Param("email") String email);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH v.location l LEFT JOIN FETCH v.profile p WHERE v.credential.id = :accountId")
    Optional<JpaVolunteer> findByAccountIdWithCredentialAndLocationAndProfile(@Param("accountId") String accountId);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c WHERE v.id = :id")
    Optional<JpaVolunteer> findById(@Param("id") String id);

    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteer p SET p.profile = :profile WHERE p.id = :id")
    Integer updateProfile(@Param("id") String id, @Param("profile") JpaProfile profile);

    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteer p SET p.location = :location WHERE p.id = :id")
    Integer updateLocation(@Param("id") String id, @Param("location") JpaLocation location);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.profile p LEFT JOIN FETCH v.credential c WHERE p.newsletter = true")
    List<JpaVolunteer> findSubscribedVolunteers();
}
