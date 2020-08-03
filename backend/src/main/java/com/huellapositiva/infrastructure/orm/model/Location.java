package com.huellapositiva.infrastructure.orm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "province")
    private String province;

    @Column(name = "town")
    private String town;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "location")
    private Set<Proposal> proposal;
}
