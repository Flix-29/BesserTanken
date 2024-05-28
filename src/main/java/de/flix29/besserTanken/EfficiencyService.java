package de.flix29.besserTanken;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class EfficiencyService {

    public FuelStation calculateMostEfficientFuelStation(double consumptionPer100Km, List<FuelStation> fuelStations) {
        var map = new HashMap<String, Double>();

        fuelStations.forEach(fuelStation -> {
            var consumption = fuelStation.getDistance().doubleValue() * (consumptionPer100Km / 100);
            var price = fuelStation.getPrice() * consumption;
            map.put(fuelStation.getId(), price);
        });

        var sorted = map.entrySet().stream()
                .sorted(Comparator.comparingDouble(HashMap.Entry::getValue))
                .map(HashMap.Entry::getKey)
                .toList();

        var first = sorted.get(0);

        List<FuelStation> list = fuelStations.stream().filter(fuelStation -> fuelStation.getId().equals(first)).toList();
        return list.get(0);

    }
}
