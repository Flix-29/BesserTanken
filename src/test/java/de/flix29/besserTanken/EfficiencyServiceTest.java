package de.flix29.besserTanken;

import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStationDetail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
class EfficiencyServiceTest {

    @Autowired
    private EfficiencyService efficiencyService;

    private static Stream<Arguments> testCalculateMostEfficientFuelStation() {
        var fuelStation1 = new FuelStation("1", anyString(), anyString(), anyString(), anyString(), new BigDecimal("2.5"), 1.559, any(FuelStationDetail.class));
        var fuelStation2 = new FuelStation("2", anyString(), anyString(), anyString(), anyString(), new BigDecimal("0.7"), 1.689, any(FuelStationDetail.class));
        var fuelStation3 = new FuelStation("3", anyString(), anyString(), anyString(), anyString(), new BigDecimal("1.2"), 1.599, any(FuelStationDetail.class));
        var fuelStation4 = new FuelStation("4", anyString(), anyString(), anyString(), anyString(), new BigDecimal("3.6"), 1.619, any(FuelStationDetail.class));
        var fuelStation5 = new FuelStation("5", anyString(), anyString(), anyString(), anyString(), new BigDecimal("4.9"), 1.659, any(FuelStationDetail.class));
        var fuelStations = List.of(fuelStation1, fuelStation2, fuelStation3, fuelStation4, fuelStation5);

        return Stream.of(
                Arguments.of(0, 0, null, Collections.emptyList()),
                Arguments.of(0, 0, Collections.emptyList(), Collections.emptyList()),
                Arguments.of(2.0, 10.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(2.0, 50.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(3.0, 10.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(3.0, 50.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(4.0, 10.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(4.0, 50.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(5.0, 10.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(5.0, 50.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(6.0, 10.0, fuelStations, List.of("2", "4", "3", "1", "5")),
                Arguments.of(6.0, 50.0, fuelStations, List.of("2", "4", "3", "1", "5"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCalculateMostEfficientFuelStation(double consumptionPer100Km, double amountGas, List<FuelStation> fuelStations, List<String> expectedIds) {
        var result = efficiencyService.calculateMostEfficientFuelStation(consumptionPer100Km, amountGas, fuelStations);
        var ids = result.keySet().stream().map(FuelStation::getId).toList();

        assertThat(ids).isEqualTo(expectedIds);
    }

}