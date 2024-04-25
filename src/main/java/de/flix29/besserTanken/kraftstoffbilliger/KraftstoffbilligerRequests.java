package de.flix29.besserTanken.kraftstoffbilliger;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import de.flix29.besserTanken.openDataSoft.OpenDataSoftRequests;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class KraftstoffbilligerRequests {

    private final KraftstoffbilligerJob kraftstoffbilligerJob;
    private final OpenDataSoftRequests openDataSoftRequests;

    public KraftstoffbilligerRequests() {
        this.kraftstoffbilligerJob = new KraftstoffbilligerJob();
        this.openDataSoftRequests = new OpenDataSoftRequests();
    }

    public List<FuelStation> getFuelStationsByPlace(String place, FuelType fuelType, Integer radius) {
        var cords = openDataSoftRequests.getCoordsFromPlzName(place);
        return getFuelStationsByLocation(cords, fuelType, radius);
    }

    public List<FuelStation> getFuelStationsByPlz(int plz, FuelType fuelType, Integer radius) {
        var cords = openDataSoftRequests.getCoordsFromPlz(plz);
        return getFuelStationsByLocation(cords, fuelType, radius);
    }

    public List<FuelStation> getFuelStationsByLocation(List<Location> locations, FuelType fuelType, Integer radius) {
        if(locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }

        return locations.stream()
                .map(Location::getCoords)
                .map(coords -> {
                    try {
                        return kraftstoffbilligerJob.getFuelStations(fuelType, coords.getLeft(), coords.getRight(), radius);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

}
