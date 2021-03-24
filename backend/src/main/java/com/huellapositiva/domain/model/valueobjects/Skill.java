package com.huellapositiva.domain.model.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Skill {

    private final String name;

    @EqualsAndHashCode.Exclude
    private final String description;

    public Skill(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
