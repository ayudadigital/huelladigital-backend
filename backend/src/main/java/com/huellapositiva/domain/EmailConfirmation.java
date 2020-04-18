package com.huellapositiva.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

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

    @CreationTimestamp
    @Column(name = "created_on")
    private Date createdOn;

    @OneToOne(mappedBy = "emailConfirmation")
    private Credential credential;
}
