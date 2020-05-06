package com.huellapositiva.infrastructure.orm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "fail_email_confirmation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailEmailConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email_address")
    private String emailAddress;
    
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "volunteer")
    private Volunteer volunteer;

    @CreationTimestamp
    @Column(name = "created_on")
    private Date createdOn;
}
