package com.huellapositiva.domain.model.valueobjects;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;

@RequiredArgsConstructor
@Data
@Builder
public class ProposalRevisionEmail {

    private final Id proposalId;

    private final String overview;

    private final EmailAddress emailAddress;

    private final Token token;

    private final URI proposalURI;

    private ProposalRevisionEmail(EmailAddress emailAddress, Token token, Id proposalId, String overview, URI proposalURI) {
        this.emailAddress = emailAddress;
        this.token = token;
        this.proposalId = proposalId;
        this.overview = overview;
        this.proposalURI = proposalURI;
    }

    public static ProposalRevisionEmail from(String email, String proposalId, String overview, URI proposalURI) {
        return new ProposalRevisionEmail(
                EmailAddress.from(email), Token.createToken(), new Id(proposalId), overview, proposalURI);
    }

    public String getEmailAddress() {
        return emailAddress.toString();
    }

    public String getToken() {
        return token.toString();
    }

    public String getOverview() {
        return this.overview;
    }

    public Id getProposalId() {
        return this.proposalId;
    }

    public URI getProposalURI() {
        return this.proposalURI;
    }

    public String getProposalURL() {
        try {
            return this.proposalURI.toURL().toExternalForm();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
