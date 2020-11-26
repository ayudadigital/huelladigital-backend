package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.Reviser;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.net.URI;

@RequiredArgsConstructor
@Data
@Builder
public class ProposalRevisionEmail {

    private final Id proposalId;

    private final String feedback;

    private final ContactPerson esalContactPerson;

    private final Token token;

    private final URI proposalURI;

    private final Reviser reviser;

    private final Boolean hasFeedback;

    public String getEmailAddress() {
        return esalContactPerson.getEmailAddress().toString();
    }

    public String getToken() {
        return token.toString();
    }

    public boolean hasFeedback(){
        return hasFeedback;
    }

    @SneakyThrows
    public String getProposalURL() {
        return this.proposalURI.toURL().toExternalForm();
    }
}
