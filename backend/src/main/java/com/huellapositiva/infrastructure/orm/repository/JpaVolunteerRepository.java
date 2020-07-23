package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaVolunteerRepository extends JpaRepository<Volunteer, Integer> {

    @Query("FROM Volunteer v LEFT JOIN FETCH v.credential c LEFT JOIN FETCH c.roles WHERE v.id = :id")
    Optional<Volunteer> findByIdWithCredentialsAndRoles(@Param("id") Integer id);

    @Query("FROM Volunteer v LEFT JOIN FETCH v.credential c WHERE v.credential.email = :email")
    Optional<Volunteer> findByEmail(@Param("email") String email);
}
