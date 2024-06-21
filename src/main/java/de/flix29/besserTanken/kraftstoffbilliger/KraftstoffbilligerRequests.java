package de.flix29.besserTanken.kraftstoffbilliger;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class KraftstoffbilligerRequests {

    private final KraftstoffbilligerJob kraftstoffbilligerJob;

    public KraftstoffbilligerRequests(KraftstoffbilligerJob kraftstoffbilligerJob) {
        this.kraftstoffbilligerJob = kraftstoffbilligerJob;
    }

    public List<FuelStation> addDetailsToFuelStations(List<FuelStation> fuelStations) {
        if (fuelStations == null || fuelStations.isEmpty()) {
            return Collections.emptyList();
        }

        return fuelStations.stream()
                .map(fuelStation -> {
                    try {
                        fuelStation.setDetails(kraftstoffbilligerJob.getFuelStationDetails(fuelStation.getId()));
                        return fuelStation;
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public List<FuelStation> getFuelStationsByLocation(List<Location> locations, FuelType fuelType, Integer radius) {
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }

        var location = locations.get(0);

        try {
            return kraftstoffbilligerJob.getFuelStations(fuelType, location.getLatitude(), location.getLongitude(), radius);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
