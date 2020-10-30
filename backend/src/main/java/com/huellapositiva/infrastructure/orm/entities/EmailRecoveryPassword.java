package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_recovery_password")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRecoveryPassword implements Serializable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "email")
        private String email;

        @Column(name = "hash", unique = true)
        private String hash;

        @CreationTimestamp
        @Column(name = "sent_on")
        private LocalDateTime sentOn;

        @OneToOne(mappedBy = "emailConfirmation")
        private JpaCredential credential;
}
