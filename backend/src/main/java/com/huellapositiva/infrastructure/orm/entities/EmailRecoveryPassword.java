package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "email_recovery_password")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRecoveryPassword implements Serializable{

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
        private JpaCredential credential;


}
