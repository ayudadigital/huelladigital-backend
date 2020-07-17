package com.huellapositiva.infrastructure.orm.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "email_confirmation")
@Getter
@Setter
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

    @UpdateTimestamp
    @Column(name = "updated_on")
    private Date updatedOn;

    @OneToOne(mappedBy = "emailConfirmation")
    private Credential credential;

    @Override
    public String toString() {
        return "EmailConfirmation{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", hash='" + hash + '\'' +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                ", credential=" + credential +
                '}';
    }
}
