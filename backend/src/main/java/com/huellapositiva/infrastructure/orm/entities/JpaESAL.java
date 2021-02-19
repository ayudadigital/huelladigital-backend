package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ESALs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaESAL implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @NaturalId
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website")
    private String website;

    @Column(name = "registered_entity", nullable = false)
    private boolean registeredEntity;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "privacy_policy", nullable = false)
    private boolean privacyPolicy;

    @Column(name = "data_protection_policy", nullable = false)
    private boolean dataProtectionPolicy;

    @JoinColumn(name = "location_id", referencedColumnName = "id", nullable = false)
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JpaLocation location;


}
