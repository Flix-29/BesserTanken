package de.flix29.besserTanken;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class EfficiencyService {

    public FuelStation calculateMostEfficientFuelStation(double consumptionPer100Km, List<FuelStation> fuelStations) {
        var map = new HashMap<FuelStation, Double>();

        fuelStations.forEach(fuelStation -> {
            var consumptionForDistance = fuelStation.getDistance().doubleValue() * (consumptionPer100Km / 100);
            var priceForDistance = fuelStation.getPrice() * consumptionForDistance;
            map.put(fuelStation, priceForDistance);
        });

        var sorted = map.entrySet().stream()
                .sorted(Comparator.comparingDouble(HashMap.Entry::getValue))
                .map(HashMap.Entry::getKey)
                .findFirst();

        return sorted.orElse(null);
    }
}
