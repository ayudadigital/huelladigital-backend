package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import lombok.Getter;

@Getter
public class AgeRange {

    private final int minimum;

    private final int maximum;

    private AgeRange(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public static AgeRange create(int minimum, int maximum) {
        validateAge(minimum, maximum);
        return new AgeRange(minimum, maximum);
    }

    private static void validateAge(int minimum, int maximum) {
        if (minimum < 18 || maximum > 80) {
            throw new InvalidProposalRequestException("Age is not in a valid range.");
        }
        if (minimum > maximum) {
            throw new InvalidProposalRequestException("Minimum age cannot be greater than maximum age.");
        }
    }
}
