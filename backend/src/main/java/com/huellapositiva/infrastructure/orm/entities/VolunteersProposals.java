package com.huellapositiva.infrastructure.orm.entities;

import java.io.Serializable;

public class VolunteersProposals implements Serializable {

    private String proposal_id;
    private String volunteer;

    public VolunteersProposals() {}

    public VolunteersProposals(String proposal_id, String volunteer) {
        this.proposal_id = proposal_id;
        this.volunteer = volunteer;
    }
}
