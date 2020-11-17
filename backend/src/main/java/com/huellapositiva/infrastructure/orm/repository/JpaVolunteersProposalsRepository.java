package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaVolunteerProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVolunteersProposalsRepository extends JpaRepository<JpaVolunteerProposal, Integer> {
    @Modifying
    @Query("UPDATE JpaVolunteerProposal v SET v.rejected = true WHERE v.volunteerId = :idVolunteer AND v.proposalId = :idProposal")
    Integer updateVolunteerInProposalRejected(@Param("idVolunteer") String idVolunteer, @Param("idProposal") String idProposal);
}
