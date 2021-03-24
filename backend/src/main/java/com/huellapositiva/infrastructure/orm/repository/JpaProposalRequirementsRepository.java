package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposalRequirements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JpaProposalRequirementsRepository extends JpaRepository<JpaProposalRequirements, Integer> {

    @Query("FROM JpaProposalRequirements r LEFT JOIN FETCH r.proposal p WHERE p.id = :id")
    List<JpaProposalRequirements> findByProposalId(@Param("id") String id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO proposal_requirements (name, proposal_id) values (:name, :proposalId)", nativeQuery = true)
    void insert(@Param("name") String name, @Param("proposalId") String proposalId);

    @Modifying
    @Transactional
    @Query("DELETE FROM JpaProposalRequirements p WHERE p.proposal.id = :proposalId")
    void deleteRequirementsByProposalId(@Param("proposalId") String proposalId);
}