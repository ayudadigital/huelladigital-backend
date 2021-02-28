package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface JpaCredentialRepository extends JpaRepository<JpaCredential, Integer> {

    @Query("FROM JpaCredential c WHERE c.id = :id")
    Optional<JpaCredential> findByAccountId(@Param("id") String id);

    @Query("FROM JpaCredential c WHERE c.email = :email")
    Optional<JpaCredential> findByEmail(@Param("email") String email);

    @Query("FROM JpaCredential c LEFT JOIN FETCH c.emailConfirmation ec WHERE ec.hash = :emailConfirmationHash")
    Optional<JpaCredential> findByEmailConfirmationHash(@Param("emailConfirmationHash") String emailConfirmationHash);

    @Modifying
    @Transactional
    @Query("UPDATE JpaCredential c SET c.hashRecoveryPassword = :hash, c.createdRecoveryHashOn = current_timestamp WHERE c.email = :email")
    Integer updateHashByEmail(@Param("email") String email, @Param("hash") String hash);

    @Query("FROM JpaCredential c WHERE c.hashRecoveryPassword = :hash")
    Optional<JpaCredential> findByHashRecoveryPassword(@Param("hash") String hash);

    @Modifying
    @Transactional
    @Query("UPDATE JpaCredential c SET c.hashedPassword = :hash WHERE c.id = :accountId")
    Integer updatePassword(@Param("hash") String hash, @Param("accountId") String accountId);

    @Modifying
    @Transactional
    @Query("UPDATE JpaCredential c SET c.hashRecoveryPassword = :hash, c.createdRecoveryHashOn = :recoveryDate WHERE c.email = :email")
    Integer setRecoveryPasswordHashAndDate(@Param("email") String email, @Param("hash") String hash, @Param("recoveryDate") Date date);
}
