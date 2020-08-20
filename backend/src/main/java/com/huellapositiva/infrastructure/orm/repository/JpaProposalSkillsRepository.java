package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposalSkills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaProposalSkillsRepository extends JpaRepository<JpaProposalSkills, Integer> {
    @Query("FROM JpaProposalSkills s LEFT JOIN FETCH s.proposal p WHERE p.id = :id")
    List<JpaProposalSkills> findByProposalId(@Param("id") String id);

}
