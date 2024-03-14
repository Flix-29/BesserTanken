package de.flix29.besserTanken.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FuelType {

    E5(1, "E5", "Super"),
    E10(2, "E10", "Super E10"),
    DIESEL(3, "Diesel", "Diesel"),
    LPG(4, "LPG", "Autogas"),
    CNG(5, "CNG", "Erdgas");

    private final int id;
    private final String name;
    private final String umgangssprachlich;

}
