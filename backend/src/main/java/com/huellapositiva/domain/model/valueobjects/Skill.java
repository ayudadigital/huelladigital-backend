package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

@Getter
public class Skill {

    private final String name;

    private final String description;

    public Skill(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
