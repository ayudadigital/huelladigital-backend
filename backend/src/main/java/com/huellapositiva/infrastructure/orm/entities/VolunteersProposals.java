package com.huellapositiva.infrastructure.orm.entities;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolunteersProposals implements Serializable {

    private String proposal;
    private String volunteer;

}
