package com.huellapositiva.infrastructure.orm.entities;


import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaCredential implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "surrogate_key")
    private Integer surrogateKey;

    @NaturalId
    @Column(name = "id")
    private String id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "email_confirmed", nullable = false)
    private Boolean emailConfirmed;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "credential_roles",
            joinColumns = {@JoinColumn(name = "credential_id", referencedColumnName = "surrogate_key")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    private Set<Role> roles;

    @OneToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "email_confirmation_id")
    private JpaEmailConfirmation emailConfirmation;

    @Column(name = "hash_recovery_password", unique = true)
    private String hashRecoveryPassword;

    @Column(name = "created_recovery_hash_on")
    private LocalDateTime createdRecoveryHashOn;
}
