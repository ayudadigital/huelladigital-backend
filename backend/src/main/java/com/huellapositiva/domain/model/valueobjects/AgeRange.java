package com.huellapositiva.domain.model.valueobjects;

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
        return new AgeRange(minimum, maximum);
    }

    public boolean isOnRange(int age) {
        return age > minimum && age < maximum;
    }
}
