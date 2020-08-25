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

    public boolean isOnRange(int age){
        return age > minimum && age < maximum;
    }

    public static AgeRange create(int minimum, int maximum){
        if(minimum < 18 || maximum > 55){
            throw new InvalidProposalRequestException("Age is not in a valid range.");
        }
        if(minimum > maximum){
            throw new InvalidProposalRequestException("Minimum age cannot be greater than maximum age.");
        }

        return new AgeRange(minimum, maximum);
    }
}
