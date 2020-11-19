package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "volunteers_proposals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaVolunteerProposal implements Serializable {

    @Id
    @Column(name = "proposal_id")
    private String proposalId;

    @Column(name = "volunteer_id")
    private String volunteerId;

    @Column(name = "rejected")
    private boolean rejected;

}
