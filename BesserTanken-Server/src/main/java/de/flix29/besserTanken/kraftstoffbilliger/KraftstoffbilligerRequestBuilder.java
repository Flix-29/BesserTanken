package de.flix29.besserTanken.kraftstoffbilliger;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import de.flix29.besserTanken.openDataSoft.OpenDataSoftRequests;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Service
public class KraftstoffbilligerRequestBuilder {

    private final KraftstoffbilligerJob kraftstoffbilligerJob;
    private final OpenDataSoftRequests openDataSoftRequests;

    public KraftstoffbilligerRequestBuilder() {
        this.kraftstoffbilligerJob = new KraftstoffbilligerJob();
        this.openDataSoftRequests = new OpenDataSoftRequests();
    }

    public List<FuelStation> getFuelStationsByPlace(String place, FuelType fuelType, int radius) {
        return getFuelStationsByPlzOrPlace(place, 0, fuelType, radius);
    }

    public List<FuelStation> getFuelStationsByPlz(int plz, FuelType fuelType, int radius) {
        return getFuelStationsByPlzOrPlace(null, plz, fuelType, radius);
    }

    public List<FuelStation> getFuelStationsByPlzOrPlace(String place, int plz, FuelType fuelType, int radius) {
        List<Location> coordsFromPlzAndPlace;
        try {
            coordsFromPlzAndPlace = openDataSoftRequests.getCoordsFromPlzsAndPlzName(plz, place);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(coordsFromPlzAndPlace == null || coordsFromPlzAndPlace.isEmpty()) {
            return null;
        }

        return coordsFromPlzAndPlace.stream()
                .map(Location::getCoords)
                .map(coords -> {
                    try {
                        return kraftstoffbilligerJob.getFuelStations(fuelType, coords.getLeft(), coords.getRight(), String.valueOf(radius));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(Collection::stream)
                .distinct()
                .sorted(FuelStation::compareTo)
                .toList();
    }

}
