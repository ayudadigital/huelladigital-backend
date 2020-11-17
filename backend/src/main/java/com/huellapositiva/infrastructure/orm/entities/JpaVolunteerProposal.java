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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @JoinColumn(name = "proposal_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private JpaProposal proposalId;

    @JoinColumn(name = "volunteer_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private JpaVolunteer volunteerId;

    @Column(name = "rejected")
    private boolean rejected;

}
