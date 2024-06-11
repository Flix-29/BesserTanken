package de.flix29.besserTanken;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EfficiencyService {

    public Map<FuelStation, Double> calculateMostEfficientFuelStation(double consumptionPer100Km, double amountGas, List<FuelStation> fuelStations) {
        var map = new HashMap<FuelStation, Double>();

        fuelStations.forEach(fuelStation -> {
            var consumptionForDistance = fuelStation.getDistance().doubleValue() * (consumptionPer100Km / 100);
            var price = fuelStation.getPrice() * (consumptionForDistance + amountGas);
            map.put(fuelStation, price);
        });

        return map.entrySet().stream()
                .sorted(Comparator.comparingDouble(HashMap.Entry::getValue))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
    }
}
