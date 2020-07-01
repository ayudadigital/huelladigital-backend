package com.huellapositiva.infrastructure.orm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

import static java.time.Instant.now;

@Entity
@Table(name = "email_confirmation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "hash", unique = true)
    private String hash;

    @UpdateTimestamp
    @Column(name = "created_on")
    private Instant createdOn;

    @OneToOne(mappedBy = "emailConfirmation")
    private Credential credential;

    @PrePersist
    protected void onPersist() {
        createdOn = now();
    }
}
