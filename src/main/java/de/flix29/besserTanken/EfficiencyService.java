package de.flix29.besserTanken;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EfficiencyService {

    public LinkedHashMap<FuelStation, Double> calculateMostEfficientFuelStation(double consumptionPer100Km, double amountGas, List<FuelStation> fuelStations) {
        if(fuelStations == null || fuelStations.isEmpty()) {
            return new LinkedHashMap<>();
        }

        var map = new LinkedHashMap<FuelStation, Double>();

        fuelStations.forEach(fuelStation -> {
            var consumptionForDistance = fuelStation.getDistance().doubleValue() * (consumptionPer100Km / 100);
            var price = fuelStation.getPrice() * (consumptionForDistance + amountGas);
            map.put(fuelStation, price);
        });

        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
