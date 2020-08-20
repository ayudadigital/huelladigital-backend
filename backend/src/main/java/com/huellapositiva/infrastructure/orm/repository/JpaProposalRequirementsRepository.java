package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposalRequirements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaProposalRequirementsRepository extends JpaRepository<JpaProposalRequirements, Integer> {

    @Query("FROM JpaProposalRequirements r LEFT JOIN FETCH r.proposal p WHERE p.id = :id")
    List<JpaProposalRequirements> findByProposalId(@Param("id") String id);

}
