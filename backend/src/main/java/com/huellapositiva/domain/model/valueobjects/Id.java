package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class Id {

    private final String value;

    public Id(String value) {
        this.value = value;
    }

    public Id(int value) {
        this.value = String.valueOf(value);
    }

    public int asInt() {
        return Integer.parseInt(value);
    }

    public static Id newId() {
        return new Id(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Id)) return false;
        Id id = (Id) o;
        return value.equals(id.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
