package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface JpaProposalRepository extends JpaRepository<JpaProposal, Integer> {

    @Query("FROM JpaProposal p LEFT JOIN FETCH p.esal LEFT JOIN FETCH p.inscribedVolunteers WHERE p.id = :id")
    Optional<JpaProposal> findByIdWithOrganizationAndInscribedVolunteers(@Param("id") String id);

    @Query("FROM JpaProposal p LEFT JOIN FETCH p.esal LEFT JOIN FETCH p.inscribedVolunteers WHERE p.id = :id")
    Optional<JpaProposal> findByNaturalId(@Param("id") String id);

    Page<JpaProposal> findByStatusIs(JpaProposalStatus status, Pageable pageable);

    Page<JpaProposal> findByStatusNot(JpaProposalStatus status, Pageable pageable);

    @Query("FROM JpaProposal p WHERE p.closingProposalDate < :currentDate AND p.status != :status")
    List<JpaProposal> findExpiredProposals(@Param("currentDate") Date date, @Param("status") JpaProposalStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE JpaProposal p SET p.status = :status WHERE p.id = :id")
    Integer updateStatusById(@Param("id") String id, @Param("status") JpaProposalStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE JpaProposal p SET p.status = :status, p.cancelReason = :reason WHERE p.id = :id")
    Integer cancelProposalById(@Param("id") String id, @Param("status") JpaProposalStatus status, @Param("reason") String reason);
}
