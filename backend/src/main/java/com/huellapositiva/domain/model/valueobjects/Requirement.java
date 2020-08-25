package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

@Getter
public class Requirement {

    private final String name;

    public Requirement(String name) {
        this.name = name;
    }
}
