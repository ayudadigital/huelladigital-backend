package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteerProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface JpaVolunteersProposalsRepository extends JpaRepository<JpaVolunteerProposal, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE JpaVolunteerProposal v SET v.rejected = true WHERE v.volunteer_id = :idVolunteer AND v.proposal_id = :idProposal")
    Integer updateVolunteerInProposalRejected(@Param("idVolunteer") String idVolunteer, @Param("idProposal") String idProposal);

    //@Query("FROM JpaVolunteerProposal v LEFT JOIN FETCH v.proposalId c WHERE v.proposalId = :id")
    //@Query("FROM JpaVolunteerProposal vp LEFT JOIN FETCH vp.volunteerId LEFT JOIN FETCH vp.proposalId p ")
    //Optional<JpaVolunteerProposal> findByIdProposal(@Param("id") String id);
}
