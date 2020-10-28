package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "volunteers")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaVolunteer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @NaturalId
    @Column(name = "id")
    private String id;

    @JoinColumn(name = "credential_id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JpaCredential credential;

    @ManyToMany(mappedBy = "inscribedVolunteers", cascade = CascadeType.ALL)
    private Set<JpaProposal> joinedProposals;

    @Column(name = "curriculum_vitae_url")
    private String curriculumVitaeUrl;
}
