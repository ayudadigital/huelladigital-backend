package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "organization_members")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "credential_id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Credential credential;

    @JoinColumn(name = "organization_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Organization joinedOrganization;
}
