package com.huellapositiva.infrastructure.orm.entities;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "credentials")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaCredential implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "email_confirmed", nullable = false)
    private Boolean emailConfirmed;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "credential_roles",
            joinColumns = {@JoinColumn(name = "credential_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    private Set<Role> roles;

    @OneToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "email_confirmation_id")
    private EmailConfirmation emailConfirmation;

    @Column(name = "hash_recovery_password", unique = true)
    private String hashRecoveryPassword;

    @Column(name = "created_recovery_hash_on")
    private LocalDateTime createdRecoveryHashOn;
}
