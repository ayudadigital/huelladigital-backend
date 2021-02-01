package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaFailEmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaFailEmailConfirmationRepository extends JpaRepository<JpaFailEmailConfirmation, Integer> {

    @Query("FROM JpaFailEmailConfirmation fec WHERE fec.emailAddress = :emailAddress")
    Optional<JpaFailEmailConfirmation> findByEmail(@Param("emailAddress") String emailAddress);
}
