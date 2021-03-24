package com.huellapositiva.domain.model.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Requirement {

    private final String name;

    public Requirement(String name) {
        this.name = name;
    }
}
