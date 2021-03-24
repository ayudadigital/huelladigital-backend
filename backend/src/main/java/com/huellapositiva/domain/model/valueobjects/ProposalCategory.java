package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.InvalidProposalCategoryException;

import java.util.Arrays;

public enum ProposalCategory {
    ON_SITE, REMOTE, MIXED;

    public static ProposalCategory getCategory(String category) {
        return Arrays.stream(ProposalCategory.values())
                .filter(s -> s.toString().equals(category))
                .findFirst()
                .orElseThrow(() -> new InvalidProposalCategoryException("Proposal category does not exist: " + category));
    }
}
