package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.InvalidStatusId;

import java.util.Arrays;

public enum ProposalStatus {
    REVIEW_PENDING (1),
    CHANGED_REQUESTED (2),
    PUBLISHED (3),
    UNPUBLISHED (4),
    FINISHED(5),
    INADEQUATE (99);

    private final int id;

    ProposalStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static ProposalStatus getStatus(int id) {
        return Arrays.stream(ProposalStatus.values())
                .filter(s -> s.getId() == id)
                .findFirst()
                .orElseThrow(InvalidStatusId::new);
    }
}
