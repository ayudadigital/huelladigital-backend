package com.huellapositiva.domain;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "volunteer")
@Builder
@NoArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "credential_id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Credential credential;
}
