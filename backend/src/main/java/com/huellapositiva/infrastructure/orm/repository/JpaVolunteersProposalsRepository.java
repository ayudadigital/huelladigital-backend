package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteerProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JpaVolunteersProposalsRepository extends JpaRepository<JpaVolunteerProposal, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteerProposal v SET v.confirmed = true WHERE v.volunteer.id = :idVolunteer AND v.proposal = :idProposal")
    Integer updateVolunteerInProposalConfirmed(@Param("idVolunteer") String idVolunteer, @Param("idProposal") String idProposal);

    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteerProposal v SET v.confirmed = false WHERE v.volunteer.id = :idVolunteer AND v.proposal = :idProposal")
    Integer updateVolunteerInProposalRejected(@Param("idVolunteer") String idVolunteer, @Param("idProposal") String idProposal);

    @Query("FROM JpaVolunteerProposal v WHERE v.volunteer.id = :idVolunteer AND v.proposal = :idProposal")
    JpaVolunteerProposal findByIdOfProposalAndVolunteer(@Param("idVolunteer") String idVolunteer, @Param("idProposal") String idProposal);

    @Query("FROM JpaVolunteerProposal vp LEFT JOIN FETCH vp.volunteer v LEFT JOIN FETCH v.credential c WHERE vp.proposal = :id")
    List<JpaVolunteerProposal> findByIdProposalWithVolunteer(@Param("id") String id);
}
