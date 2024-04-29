package de.flix29.besserTanken;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.testbench.unit.UIUnitTest;
import de.flix29.besserTanken.kraftstoffbilliger.KraftstoffbilligerRequests;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BesserTankenViewTest extends UIUnitTest {

    private BesserTankenView besserTankenView;
    @MockBean
    private KraftstoffbilligerRequests kraftstoffbilligerRequests;

    @BeforeEach
    void setUp() {
        besserTankenView = navigate(BesserTankenView.class);
    }

    @Test
    void initialSetup_success() {
        assertThat(besserTankenView)
                .isNotNull()
                .hasFieldOrProperty("radiusField")
                .hasFieldOrProperty("placeField")
                .hasFieldOrProperty("useCurrentLocationSelect")
                .hasFieldOrProperty("fuelTypeSelect")
                .hasFieldOrProperty("orderBySelect")
                .hasFieldOrProperty("searchButton");

        assertThat(besserTankenView.radiusField)
                .isNotNull()
                .isInstanceOf(TextField.class)
                .satisfies(field -> {
                    assertThat(field.getLabel())
                            .isEqualTo("Enter radius (km): ");
                    assertThat(field.getPlaceholder())
                            .isEqualTo("5");
                    assertThat(field.getValue())
                            .isEqualTo("5");
                });

        assertThat(besserTankenView.placeField)
                .isNotNull()
                .isInstanceOf(TextField.class)
                .satisfies(field -> {
                    assertThat(field.getLabel())
                            .isEqualTo("Place or plz: ");
                    assertThat(field.getPlaceholder())
                            .isEqualTo("'Berlin' or '10178'");
                    assertThat(field.getValue())
                            .isEmpty();
                });

        assertThat(besserTankenView.useCurrentLocationSelect)
                .isNotNull()
                .isInstanceOf(Select.class)
                .satisfies(select -> {
                    assertThat(select.getLabel())
                            .isEqualTo("Select search type: ");
                    assertThat(select.getValue())
                            .isEqualTo("Use plz/place");
                });

        assertThat(besserTankenView.fuelTypeSelect)
                .isNotNull()
                .isInstanceOf(Select.class)
                .satisfies(select -> {
                    assertThat(select.getLabel())
                            .isEqualTo("Select fuel type: ");
                    assertThat(select.getValue())
                            .isEqualTo(FuelType.DIESEL.getName());
                });

        assertThat(besserTankenView.orderBySelect)
                .isNotNull()
                .isInstanceOf(Select.class)
                .satisfies(select -> {
                    assertThat(select.getLabel())
                            .isEqualTo("Order by: ");
                    assertThat(select.getValue())
                            .isEqualTo("Price");
                });

        assertThat(besserTankenView.searchButton)
                .isNotNull()
                .isInstanceOf(Button.class)
                .extracting(Button::getText)
                .isEqualTo("Search");
    }

    static Stream<Arguments> changeSearchType_success() {
        return Stream.of(
                Arguments.of("Use plz/place", "Use plz/place", "", true, ""),
                Arguments.of("Use plz/place", "Use location", "", false, ""),
                Arguments.of("Use plz/place", "Use plz/place", "Berlin", true, "Berlin"),
                Arguments.of("Use plz/place", "Use location", "Berlin", false, ""),
                Arguments.of("Use location", "Use plz/place", "", true, ""),
                Arguments.of("Use location", "Use location", "", false, ""),
                Arguments.of("Use location", "Use plz/place", "Berlin", true, ""),
                Arguments.of("Use location", "Use location", "Berlin", false, "Berlin")
        );
    }

    @ParameterizedTest
    @MethodSource
    void changeSearchType_success(String initialSearchType, String newSearchType, String place, boolean expectedPlaceFieldVisibility, String expectedPlaceValue) {
        besserTankenView.useCurrentLocationSelect.setValue(initialSearchType);
        besserTankenView.placeField.setValue(place);
        besserTankenView.useCurrentLocationSelect.setValue(newSearchType);

        assertThat(besserTankenView.placeField)
                .satisfies(field -> {
                    assertThat(field.getValue())
                            .isEqualTo(expectedPlaceValue);
                    assertThat(field.isVisible())
                            .isEqualTo(expectedPlaceFieldVisibility);
                });
    }

    static Stream<Arguments> searchButtonClicked_success() {
        return Stream.of(
                Arguments.of("Use plz/place", any(Location.class), "Berlin", 10178, FuelType.DIESEL, 5, List.of()),
                Arguments.of("Use location", any(Location.class), "", 10178, FuelType.DIESEL, 5, List.of())
        );
    }

    @ParameterizedTest
    @MethodSource
    void searchButtonClicked_success(String searchType, Location location, String place, int plz, FuelType fuelType, int radius, List<FuelStation> fuelStations) {
        besserTankenView.useCurrentLocationSelect.setValue(searchType);
        besserTankenView.currentLocation = location;
        besserTankenView.placeField.setValue(place == null ? String.valueOf(plz) : place);
        besserTankenView.fuelTypeSelect.setValue(fuelType.getName());
        besserTankenView.radiusField.setValue(String.valueOf(radius));

        //TODO: Mock the environment to test the search button click
        besserTankenView.searchButton.click();

        when(kraftstoffbilligerRequests.getFuelStationsByPlz(any(), any(), any())).thenReturn(fuelStations);
        when(kraftstoffbilligerRequests.getFuelStationsByPlace(any(), any(), any())).thenReturn(fuelStations);
        when(kraftstoffbilligerRequests.getFuelStationsByLocation(any(), any(), any())).thenReturn(fuelStations);


    }

}