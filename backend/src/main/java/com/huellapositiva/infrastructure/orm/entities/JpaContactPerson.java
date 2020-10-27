package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "contact_persons")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaContactPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @Column(name = "id")
    private String id;

    @JoinColumn(name = "credential_id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private JpaCredential credential;

    @JoinColumn(name = "esal_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private JpaESAL joinedEsal;
}
