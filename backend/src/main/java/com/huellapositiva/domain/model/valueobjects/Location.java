package com.huellapositiva.domain.model.valueobjects;

import lombok.Getter;

@Getter
public class Location {

    private final Id id;

    private final String province;

    private final String town;

    private final String address;

    private final String zipCode;

    public Location(String province, String town, String address, String zipCode) {
        this.zipCode = zipCode;
        this.id = Id.newId();
        this.province = province;
        this.town = town;
        this.address = address;
    }
}
