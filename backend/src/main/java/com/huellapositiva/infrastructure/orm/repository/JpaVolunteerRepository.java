package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaVolunteerRepository extends JpaRepository<JpaVolunteer, Integer> {

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH c.roles WHERE v.id = :id")
    Optional<JpaVolunteer> findByIdWithCredentialsAndRoles(@Param("id") String id);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c WHERE v.credential.email = :email")
    Optional<JpaVolunteer> findByEmail(@Param("email") String email);

    @Query("FROM JpaVolunteer v LEFT JOIN FETCH v.credential c WHERE v.id = :id")
    Optional<JpaVolunteer> findById(@Param("id") String id);

    @Modifying
    @Query("UPDATE JpaVolunteer v SET v.curriculumVitaeUrl = :cvUrl WHERE v.id = :id")
    Integer updateCurriculumVitae(@Param("id") String id, @Param("cvUrl") String cvUrl);
/*
    @Modifying
    @Query("UPDATE JpaVolunteer v LEFT JOIN FETCH v.credential c SET v.subscribed = true WHERE c.email = :email")
    Integer updateToSubscribed(@Param("email") String email);

    @Modifying
    @Query("UPDATE JpaVolunteer v LEFT JOIN FETCH v.credential c SET v.subscribed = false WHERE c.email = :email")
    Integer updateToNotSubscribed(@Param("email") String email);

 */
}
