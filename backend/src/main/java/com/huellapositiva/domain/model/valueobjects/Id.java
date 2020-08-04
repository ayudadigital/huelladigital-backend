package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

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
}
