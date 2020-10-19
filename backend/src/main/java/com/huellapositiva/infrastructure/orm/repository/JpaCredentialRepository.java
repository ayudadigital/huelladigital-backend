package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface JpaCredentialRepository extends JpaRepository<JpaCredential, Integer> {

    @Query("FROM JpaCredential c WHERE c.email = :email")
    Optional<JpaCredential> findByEmail(@Param("email") String email);

    @Query("FROM JpaCredential c LEFT JOIN FETCH c.emailConfirmation ec WHERE ec.hash = :emailConfirmationHash")
    Optional<JpaCredential> findByEmailConfirmationHash(@Param("emailConfirmationHash") String emailConfirmationHash);

    @Modifying
    @Transactional
    @Query("UPDATE JpaCredential c SET c.hash_recovery_password = :hash, c.created_recovery_hash_on = current_timestamp WHERE c.email = :email")
    Integer updateHashByEmail(@Param("email") String email, @Param("hash") String hash);
}
