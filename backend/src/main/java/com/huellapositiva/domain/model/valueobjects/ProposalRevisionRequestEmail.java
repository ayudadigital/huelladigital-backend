package com.huellapositiva.domain.model.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class ProposalRevisionRequestEmail {

    private final String proposalToReviseUrl;
    private final EmailAddress emailAddress;
    private final Token token;

    private ProposalRevisionRequestEmail(EmailAddress emailAddress, Token token, String proposalToReviseUrl) {
        this.emailAddress = emailAddress;
        this.token = token;
        this.proposalToReviseUrl = proposalToReviseUrl;
    }

    public static ProposalRevisionRequestEmail from(String email, String proposalToReviseUrl) {
        return new ProposalRevisionRequestEmail(
                EmailAddress.from(email), Token.createToken(), proposalToReviseUrl);
    }

    public String getEmailAddress() {
        return emailAddress.toString();
    }

    public String getToken() {
        return token.toString();
    }

    public String getProposalUrl() {
        return proposalToReviseUrl;
    }
}
