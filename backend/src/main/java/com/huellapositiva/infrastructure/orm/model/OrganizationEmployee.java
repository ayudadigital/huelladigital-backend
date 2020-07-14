package com.huellapositiva.infrastructure.orm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "organization_users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationEmployee {

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
