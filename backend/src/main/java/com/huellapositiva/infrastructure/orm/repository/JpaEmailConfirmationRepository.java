package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaEmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface JpaEmailConfirmationRepository extends JpaRepository<JpaEmailConfirmation, Integer> {

    @Query("FROM JpaEmailConfirmation ec LEFT JOIN FETCH ec.credential WHERE ec.hash = :hash")
    Optional<JpaEmailConfirmation> findByHash(@Param("hash") String hash);

    @Query("FROM JpaEmailConfirmation ec LEFT JOIN FETCH ec.credential WHERE ec.email = :email")
    Optional<JpaEmailConfirmation> findByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE JpaEmailConfirmation ec SET ec.hash = :hash, ec.updatedOn = current_timestamp WHERE ec.email = :email")
    Integer updateHashByEmail(@Param("email") String email, @Param("hash") String hash);
}
