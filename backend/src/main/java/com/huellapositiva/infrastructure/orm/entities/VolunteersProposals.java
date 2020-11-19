package com.huellapositiva.infrastructure.orm.entities;

import java.io.Serializable;

public class VolunteersProposals implements Serializable {

    private String proposal_id;
    private String volunteer_id;

    public VolunteersProposals() {}

    public VolunteersProposals(String proposal_id, String volunteer_id) {
        this.proposal_id = proposal_id;
        this.volunteer_id = volunteer_id;
    }
}
