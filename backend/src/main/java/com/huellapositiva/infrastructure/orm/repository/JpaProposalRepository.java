package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaProposalRepository extends JpaRepository<JpaProposal, Integer> {

    @Query("FROM JpaProposal p LEFT JOIN FETCH p.esal LEFT JOIN FETCH p.inscribedVolunteers WHERE p.id = :id")
    Optional<JpaProposal> findByIdWithOrganizationAndInscribedVolunteers(@Param("id") String id);

    @Query("FROM JpaProposal p LEFT JOIN FETCH p.esal LEFT JOIN FETCH p.inscribedVolunteers WHERE p.id = :id")
    Optional<JpaProposal> findByNaturalId(@Param("id") String id);

    Page<JpaProposal> findByStatusIs(JpaStatus status, Pageable pageable);

    Page<JpaProposal> findByStatusNot(JpaStatus status, Pageable pageable);

}
