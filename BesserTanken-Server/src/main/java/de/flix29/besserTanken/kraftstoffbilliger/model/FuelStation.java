package de.flix29.besserTanken.kraftstoffbilliger.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuelStation {

    private String id;
    private String brand;
    private String name;
    private String address;
    private String city;
    private double distance;
    private String lat;
    private String lon;
    private double price;

}
