package com.huellapositiva.infrastructure.orm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "volunteer")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "credential_id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Credential credential;

    @ManyToMany(mappedBy = "joinedVolunteers")
    private List<Proposal> joinedProposals;
}
