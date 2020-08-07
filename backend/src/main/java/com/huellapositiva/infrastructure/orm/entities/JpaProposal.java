package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "proposals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @Column(name = "id")
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @JoinColumn(name = "esal_id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private JpaESAL esal;

    @JoinColumn(name = "location_id")
    @ManyToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Location location;

    @Column(name = "required_days", nullable = false)
    private String requiredDays;

    @Column(name = "min_age", nullable = false)
    private Integer minimumAge;

    @Column(name = "max_age", nullable = false)
    private Integer maximumAge;

    @Column(name = "expiration_date", nullable = false)
    private Date expirationDate;

    @Column(name = "published")
    private Boolean published;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "volunteers_proposals",
            joinColumns = {@JoinColumn(name = "proposal_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "volunteer_id", referencedColumnName = "id")}
    )
    private Set<JpaVolunteer> inscribedVolunteers;
}
