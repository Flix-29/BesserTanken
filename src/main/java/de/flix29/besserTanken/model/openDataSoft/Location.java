package de.flix29.besserTanken.model.openDataSoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location extends SimpleLocation{

    private int plz;
    private String name;

    public Location(int plz, String name, double latitude, double longitude) {
        super(latitude, longitude);
        this.plz = plz;
        this.name = name;
    }

}
