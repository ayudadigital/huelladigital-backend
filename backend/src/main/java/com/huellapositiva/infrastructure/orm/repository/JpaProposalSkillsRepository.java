package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposalSkills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JpaProposalSkillsRepository extends JpaRepository<JpaProposalSkills, Integer> {

    @Query("FROM JpaProposalSkills s LEFT JOIN FETCH s.proposal p WHERE p.id = :id")
    List<JpaProposalSkills> findByProposalId(@Param("id") String id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO proposal_skills (name, description, proposal_id) values (:name, :description, :proposalId)", nativeQuery = true)
    void insert(@Param("name") String name, @Param("description") String description, @Param("proposalId") String proposalId);

    @Modifying
    @Transactional
    @Query("DELETE FROM JpaProposalSkills p WHERE p.proposal.id = :proposalId")
    void deleteSkillByProposalId(@Param("proposalId") String proposalId);
}
