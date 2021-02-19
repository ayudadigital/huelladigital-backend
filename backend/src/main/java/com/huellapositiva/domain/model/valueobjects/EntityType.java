package com.huellapositiva.domain.model.valueobjects;

public enum EntityType {
    ASOCIACION,
    FUNDACION,
    FEDERACION,
    DEPORTIVA,
    COLEGIO,
    PROFESIONAL,
    CLUB;

    public static EntityType getValue(String value) {
        try {
            return EntityType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid entity type value: " + value);
        }
    }

}


