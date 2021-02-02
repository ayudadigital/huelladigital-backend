package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaReviser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaReviserRepository extends JpaRepository<JpaReviser, Integer> {

    @Query("FROM JpaReviser r LEFT JOIN FETCH r.credential cr WHERE cr.id = :accountId")
    Optional<JpaReviser> findByAccountIdWithCredentials(@Param("accountId") String accountId);
}
