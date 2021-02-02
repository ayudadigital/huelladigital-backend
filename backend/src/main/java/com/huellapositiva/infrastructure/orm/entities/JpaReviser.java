package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "reviser")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaReviser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @NaturalId
    @Column(name = "id")
    private String id;

    @JoinColumn(name = "credential_id", referencedColumnName = "id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JpaCredential credential;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;
}
