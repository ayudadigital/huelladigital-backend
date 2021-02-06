package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.entities.Reviser;
import lombok.*;

import java.net.URI;

@Data
@Builder
@AllArgsConstructor
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
