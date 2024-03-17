package de.flix29.besserTanken.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuelStationDetail {

    private String brand;
    private String name;
    private String address;
    private String city;
    private String distance;
    private List<Price> prices;
    private List<OpeningTime> opening;
    private String lat;
    private String lon;

}
