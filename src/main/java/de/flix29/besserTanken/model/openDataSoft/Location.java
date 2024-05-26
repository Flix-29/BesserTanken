package de.flix29.besserTanken.model.openDataSoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private int plz;
    private String name;
    private double latitude;
    private double longitude;

}
