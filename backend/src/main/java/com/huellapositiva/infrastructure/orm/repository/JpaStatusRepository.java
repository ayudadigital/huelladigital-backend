package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaStatusRepository extends JpaRepository<JpaProposalStatus, Integer> {

    @Query("FROM JpaProposalStatus s WHERE s.id = :id")
    Optional<JpaProposalStatus> findById(@Param("id") Integer id);

}
