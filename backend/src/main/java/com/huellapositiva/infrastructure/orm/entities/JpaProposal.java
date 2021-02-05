package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;
import org.hibernate.annotations.NaturalId;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "proposals")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaProposal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @NaturalId
    @Column(name = "id")
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @JoinColumn(name = "esal_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private JpaESAL esal;

    @JoinColumn(name = "location_id", referencedColumnName = "id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private JpaLocation location;

    @Column(name = "required_days", nullable = false)
    private String requiredDays;

    @Column(name = "min_age", nullable = false)
    private Integer minimumAge;

    @Column(name = "max_age", nullable = false)
    private Integer maximumAge;

    @Column(name = "starting_proposal_date")
    private Date startingProposalDate;

    @Column(name = "closing_proposal_date", nullable = false)
    private Date closingProposalDate;

    @Column(name = "starting_volunteering_date")
    private Date startingVolunteeringDate;

    @JoinColumn(name = "status_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private JpaProposalStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "duration_in_days")
    private String durationInDays;

    @Column(name = "category")
    private String category;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "volunteers_proposals",
            joinColumns = {@JoinColumn(name = "proposal_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "volunteer_id", referencedColumnName = "id")}
    )
    private Set<JpaVolunteer> inscribedVolunteers;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "proposal")
    private Set<JpaProposalSkills> skills;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "proposal")
    private Set<JpaProposalRequirements> requirements;

    @Column(name = "extra_info")
    private String extraInfo;

    @Column(name = "instructions")
    private String instructions;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "cancel_reason")
    private String cancelReason;
}
