package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaProposalRepository extends JpaRepository<Proposal, Integer> {

    @Query("FROM Proposal p LEFT JOIN FETCH p.organization LEFT JOIN FETCH p.joinedVolunteers WHERE p.id = :id")
    Optional<Proposal> findByIdWithOrganizationAndInscribedVolunteers(@Param("id") Integer id);
}
