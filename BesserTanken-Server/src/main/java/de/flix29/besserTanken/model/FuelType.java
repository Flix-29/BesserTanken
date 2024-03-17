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
    private final String de_umgangssprachlich;

    public static FuelType fromId(int id) throws IllegalArgumentException{
        for (FuelType fuelType : FuelType.values()) {
            if (fuelType.getId() == id) {
                return fuelType;
            }
        }
        throw new IllegalArgumentException("Invalid fuel type id: " + id);
    }

    public static FuelType fromName(String name) throws IllegalArgumentException{
        for (FuelType fuelType : FuelType.values()) {
            if (fuelType.getName().equals(name)) {
                return fuelType;
            }
        }
        throw new IllegalArgumentException("Invalid fuel type name: " + name);
    }

    public static FuelType fromDeUmgangssprachlich(String de_umgangssprachlich) throws IllegalArgumentException{
        for (FuelType fuelType : FuelType.values()) {
            if (fuelType.getDe_umgangssprachlich().equals(de_umgangssprachlich)) {
                return fuelType;
            }
        }
        throw new IllegalArgumentException("Invalid fuel type de_umgangssprachlich: " + de_umgangssprachlich);
    }

}
