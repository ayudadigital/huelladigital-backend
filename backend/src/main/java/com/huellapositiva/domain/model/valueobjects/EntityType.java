package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.InvalidEntityTypeException;

public enum EntityType {
    ASSOCIATION,
    FOUNDATION,
    SPORTS_FEDERATION,
    COLLEGE_PROFESSIONAL,
    SPORTS_CLUB;

    public static EntityType getValue(String value) {
        try {
            return EntityType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidEntityTypeException("Invalid entity type value: " + value);
        }
    }

}


