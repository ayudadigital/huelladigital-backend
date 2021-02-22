package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.InvalidStatusIdException;

import java.util.Arrays;

public enum ProposalStatus {
    REVIEW_PENDING (1),
    CHANGES_REQUESTED (2),
    PUBLISHED (3),
    ENROLLMENT_CLOSED (4),
    FINISHED(5),
    CANCELLED(6),
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
                .orElseThrow(InvalidStatusIdException::new);
    }
}
