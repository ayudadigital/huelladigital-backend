package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaCredentialRepository extends JpaRepository<Credential, Integer> {

    @Query("FROM Credential c WHERE c.email = :email")
    Optional<Credential> findByEmail(@Param("email") String email);

    @Query("FROM Credential c LEFT JOIN FETCH c.emailConfirmation ec WHERE ec.hash = :emailConfirmationHash")
    Optional<Credential> findByEmailConfirmationHash(@Param("emailConfirmationHash") String emailConfirmationHash);
}
