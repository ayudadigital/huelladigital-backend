package com.huellapositiva.infrastructure.orm.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ESALs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JpaESAL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer surrogateKey;

    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;
}
