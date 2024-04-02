package de.flix29.besserTanken.model.openDataSoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private int plz;
    private String name;
    private Pair<Double, Double> coords;

}
