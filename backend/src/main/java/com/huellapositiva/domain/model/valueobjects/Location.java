package com.huellapositiva.domain.model.valueobjects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;

@Getter
@Setter
public class Location {

    private final Id id;

    private String province;

    private String town;

    private String address;

    private String zipCode;

    private String island;

    @Builder
    public Location(String province, String town, String address, String zipCode, String island) {
        this.zipCode = zipCode;
        this.id = Id.newId();
        this.province = province;
        this.town = town;
        this.address = address;
        this.island = island;
    }

    public static boolean isNotIsland(String island) {

        final HashSet<String> islands = new HashSet<>(
                Arrays.asList("La Graciosa", "Lanzarote", "Fuerteventura", "Gran Canaria",
                        "Tenerife", "La Gomera", "La Palma", "El Hierro")
        );

        for (String i: islands) {
            if (i.equals(island)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotZipCode(String zipCode) {
        final String lasPalmasZipCode = "35";
        final String santaCruzZipCode = "38";
        final String firstTwoCharacters = zipCode.substring(0, 2);
        final boolean isZipCode = lasPalmasZipCode.equals(firstTwoCharacters) || santaCruzZipCode.equals(firstTwoCharacters);

        return !(isNumeric(zipCode) && isZipCode);
    }

    private static boolean isNumeric(String s) { return s.matches("\\d*");
    }
}
