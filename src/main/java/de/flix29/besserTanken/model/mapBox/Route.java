package de.flix29.besserTanken.model.mapBox;

import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    private double weightTypical;
    private double durationTypical;
    private String weightName;
    private double weight;
    private double duration;
    private double distance;
    //Not implemented
    private List<String> legs;
    private List<SimpleLocation> geometry;

}
