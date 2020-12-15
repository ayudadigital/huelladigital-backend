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

    @JoinColumn(name = "location_id", referencedColumnName = "id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JpaLocation location;

    @ManyToMany(mappedBy = "inscribedVolunteers", cascade = CascadeType.ALL)
    private Set<JpaProposal> joinedProposals;

    @Column(name = "curriculum_vitae_url")
    private String curriculumVitaeUrl;

    @Column(name = "twitter")
    private String twitter;

    @Column(name = "instagram")
    private String instagram;

    @Column(name = "linkedin")
    private String linkedin;

    @Column(name = "photo")
    private String photoUrl;

    @Column(name = "additional_information")
    private String additionalInformation;
}
