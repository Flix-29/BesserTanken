package de.flix29.besserTanken;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class EfficiencyService {

    public FuelStation calculateMostEfficientFuelStation(double consumption, List<FuelStation> fuelStations) {
        var sortedFuelStations = fuelStations.stream()
                .filter(Objects::nonNull)
                .filter(fuelStation -> fuelStation.getPrice() != 0.0)
                .sorted((fuelStation1, fuelStation2) -> fuelStation2.getDistance().compareTo(fuelStation1.getDistance()))
                .toList();

        var minPrice = sortedFuelStations.stream()
                .min(Comparator.comparingDouble(FuelStation::getPrice))
                .get();

        var maxPrice = sortedFuelStations.stream()
                .min(Comparator.comparingDouble(FuelStation::getPrice))
                .get();

        if (sortedFuelStations.get(0).equals(minPrice)) {
            return sortedFuelStations.get(0);
        }
        return sortedFuelStations.get(0);
    }
}
