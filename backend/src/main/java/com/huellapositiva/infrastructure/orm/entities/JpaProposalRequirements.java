package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "proposal_requirements")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaProposalRequirements implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @Column(name = "name")
    private String name;

    @JoinColumn(name = "proposal_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private JpaProposal proposal;

}
