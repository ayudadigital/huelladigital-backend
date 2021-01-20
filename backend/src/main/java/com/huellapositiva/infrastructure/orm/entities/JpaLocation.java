package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaLocation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @NaturalId
    private String id;

    @Column(name = "province")
    private String province;

    @Column(name = "town")
    private String town;

    @Column(name = "address")
    private String address;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "island")
    private String island;

    @OneToMany(mappedBy = "location")
    private Set<JpaProposal> proposal;
}
