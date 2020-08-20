package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "proposal_skills")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaProposalSkills implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @JoinColumn(name = "proposal_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private JpaProposal proposal;

}
