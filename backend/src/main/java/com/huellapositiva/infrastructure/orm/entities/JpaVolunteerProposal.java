package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(VolunteersProposals.class)
@Table(name = "volunteers_proposals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaVolunteerProposal implements Serializable {

    @Id
    @Column(name = "proposal_id")
    private String proposal;

    @Id
    @JoinColumn(name = "volunteer_id", referencedColumnName = "id")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private JpaVolunteer volunteer;

    @Column(name = "confirmed")
    private boolean confirmed;
}
