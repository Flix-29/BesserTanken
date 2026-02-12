package de.flix29.besserTanken;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.flix29.BesserTanken;
import de.flix29.besserTanken.kraftstoffbilliger.KraftstoffbilligerRequests;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;
import de.flix29.besserTanken.openDataSoft.OpenDataSoftRequests;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@PermitAll
@Route(value = "")
@PageTitle("BesserTanken")
public class BesserTankenView extends Div {

    private static final String HORIZONTAL_LAYOUT = "horizontal-layout";
    private static final String USE_PLZ_PLACE = "Use plz/place";
    private static final String EFFICIENCY_CALC = "efficiencyCalc";
    private final KraftstoffbilligerRequests kraftstoffbilligerRequests;
    private final OpenDataSoftRequests openDataSoftRequests;
    private final EfficiencyService efficiencyService;

    private final Div fuelStationsLayout = new Div();
    private final Div efficiencyLayout = new Div();

    private List<FuelStation> foundFuelStations;
    private List<FuelStation> displayedFuelStations;
    private boolean useCurrentLocation;
    private SimpleLocation currentLocation;

    private final NumberField radiusField;
    private final Select<String> useCurrentLocationSelect;
    private final Select<String> orderBySelect;
    private final Select<String> resultLimitSelect;
    private final Select<String> fuelTypeSelect;
    private final TabSheet tabSheet;

    public BesserTankenView(
            KraftstoffbilligerRequests kraftstoffbilligerRequests,
            OpenDataSoftRequests openDataSoftRequests,
            EfficiencyService efficiencyService
    ) {
        this.kraftstoffbilligerRequests = kraftstoffbilligerRequests;
        this.openDataSoftRequests = openDataSoftRequests;
        this.efficiencyService = efficiencyService;

        radiusField = new NumberField("Enter radius (km): ", "5");
        radiusField.setSuffixComponent(new Div("km"));

        var placeField = new TextField("Place or plz: ", "'Berlin' or '10178'");

        useCurrentLocationSelect = new Select<>(event -> {
            useCurrentLocation = event.getValue().equals("Use location");
            placeField.setValue("");
            placeField.setVisible(!useCurrentLocation);
            if (useCurrentLocation) {
                getCurrentLocation();
            } else {
                currentLocation = null;
            }
        });
        useCurrentLocationSelect.setItems("Use location", USE_PLZ_PLACE);
        useCurrentLocationSelect.setLabel("Select search type: ");
        useCurrentLocationSelect.setValue(USE_PLZ_PLACE);

        fuelTypeSelect = new Select<>();
        fuelTypeSelect.setItems(Arrays.stream(FuelType.values())
                .map(FuelType::getName)
                .toArray(String[]::new));
        fuelTypeSelect.setLabel("Select fuel type: ");
        fuelTypeSelect.setValue(FuelType.DIESEL.getName());

        resultLimitSelect = new Select<>(event -> displayFuelStations());
        resultLimitSelect.setItems("10", "25", "50", "all");
        resultLimitSelect.setLabel("Result limit:");
        resultLimitSelect.setValue("10");
        resultLimitSelect.getStyle().setMargin("0px");

        orderBySelect = new Select<>(event -> displayFuelStations());
        orderBySelect.setItems("Price", "Distance");
        orderBySelect.setLabel("Order by: ");
        orderBySelect.setValue("Price");
        orderBySelect.getStyle().setMargin("0px");

        var searchButton = new Button("Search", event -> {
            foundFuelStations = performSearch(
                    placeField.getValue(),
                    FuelType.fromName(fuelTypeSelect.getValue()),
                    radiusField.getValue() == null ? 0 : (int) Math.round(radiusField.getValue())
            );
            displayFuelStations();
        });
        searchButton.addClickShortcut(Key.ENTER);

        var orderByLimitLayout = new Div(resultLimitSelect, orderBySelect);
        orderByLimitLayout.addClassName(HORIZONTAL_LAYOUT);
        orderByLimitLayout.getStyle().setJustifyContent(Style.JustifyContent.END);
        orderByLimitLayout.getStyle().setMarginLeft("auto");

        var filterDiv = new Div(
                useCurrentLocationSelect,
                placeField,
                fuelTypeSelect,
                radiusField,
                searchButton,
                orderByLimitLayout
        );
        filterDiv.addClassName(HORIZONTAL_LAYOUT);
        filterDiv.getStyle().setFlexBasis(Style.FlexBasis.AUTO);
        filterDiv.getStyle().setAlignItems(Style.AlignItems.CENTER);
        searchButton.getStyle().setAlignSelf(Style.AlignSelf.END);

        var tab1 = new Tab(FontAwesome.Solid.GAS_PUMP.create(), new Span("Fuel Stations"));
        tab1.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab1.addClassNames("FuelStations", "tab-item");
        var tab3 = new Tab(FontAwesome.Solid.STOPWATCH.create(), new Span("Efficiency calculator"));
        tab3.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab3.addClassNames("Map", "tab-item");

        tabSheet = new TabSheet();
        tabSheet.add(tab1, fuelStationsLayout);
        tabSheet.add(tab3, new LazyComponent(() -> efficiencyLayout));
        tabSheet.getStyle().setMarginTop("30px");
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
        tabSheet.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(tab3)) {
                renderEfficiencyCalc();
            }
        });

        var besserTankenName = new H1("BesserTanken");
        besserTankenName.getStyle().setMargin("0px");

        var version = new Span("v" + BesserTanken.getEnv().getProperty("bessertanken.version"));
        version.getStyle().setFontSize("var(--lumo-font-size-s)");
        version.getStyle().setColor("var(--lumo-contrast-70pct)");
        version.getStyle().setMarginBottom("4px");

        var header = new Div(besserTankenName, version);
        header.getStyle().setAlignItems(Style.AlignItems.END);
        header.getStyle().setMargin("15px 0px 30px 0px");
        header.addClassNames(HORIZONTAL_LAYOUT, "header-layout");

        add(
                header,
                filterDiv,
                new Hr(),
                tabSheet
        );
    }

    private void getCurrentLocation() {
        log.info("Trying to get current location.");
        try {
            String javascript = Files.readString(Path.of("src/main/javascript/Geolocator.js"));
            UI.getCurrent().getPage().executeJs(javascript, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ClientCallable
    @SuppressWarnings("unused")
    private void receiveCoords(Double[] coords) {
        if (coords == null || coords.length != 2) {
            log.warn("Received invalid coordinates.");
            currentLocation = null;
            useCurrentLocation = false;
            useCurrentLocationSelect.setValue(USE_PLZ_PLACE);
            return;
        }

        var location = new SimpleLocation();
        location.setLatitude(coords[0]);
        location.setLongitude(coords[1]);
        currentLocation = location;
    }

    private List<FuelStation> performSearch(String place, FuelType fuelType, Integer radius) {
        foundFuelStations = new ArrayList<>();
        if (!place.isEmpty()) {
            try {
                var plz = Integer.parseInt(place);
                log.info("Searching coords for plz: {}", plz);
                currentLocation = openDataSoftRequests.getCoordsFromPlz(plz);
            } catch (NumberFormatException e) {
                log.info("Searching coords for place: {}", place);
                currentLocation = openDataSoftRequests.getCoordsFromPlzName(place);
            }
        }

        if (currentLocation == null) {
            log.warn("Please fill in a place or plz or agree to use your location.");
            return Collections.emptyList();
        }

        log.info("Searching location: {} with fuel type: {} and radius: {}.", currentLocation, fuelType, radius);
        foundFuelStations = kraftstoffbilligerRequests.getFuelStationsByLocation(currentLocation, fuelType, radius);
        log.info("Found {} fuel stations.", foundFuelStations.size());

        return foundFuelStations;
    }

    private void displayFuelStations() {
        removeComponentsByClassName(fuelStationsLayout, "temp");

        if (foundFuelStations == null) return;

        if (!foundFuelStations.isEmpty()) {
            int limit;
            if (!resultLimitSelect.getValue().equals("all")) {
                limit = Integer.parseInt(resultLimitSelect.getValue());
            } else {
                limit = foundFuelStations.size();
            }

            displayedFuelStations = foundFuelStations.stream()
                    .filter(fuelStation -> fuelStation.getPrice() != 0.0)
                    .sorted((fuelStation1, fuelStation2) -> {
                        if (orderBySelect.getValue().equals("Distance")) {
                            return fuelStation1.getDistance().compareTo(fuelStation2.getDistance());
                        } else {
                            return Double.compare(fuelStation1.getPrice(), fuelStation2.getPrice());
                        }
                    })
                    .limit(limit)
                    .toList();

            displayedFuelStations = kraftstoffbilligerRequests.addDetailsToFuelStations(displayedFuelStations);
            displayedFuelStations.forEach(fuelStation -> {
                var name = new H3(fuelStation.getName());
                name.addClassName("text-wrap");

                var address = new Paragraph(fuelStation.getAddress() + ", " + fuelStation.getCity());
                address.getStyle().setFontSize("var(--lumo-font-size-m)");

                var price = new H1(fuelStation.getPrice() + "€");
                price.setWidth("max-content");

                var distance = new Paragraph(fuelStation.getDistance() + " km");
                distance.setWidth("max-content");
                distance.getStyle().setFontSize("var(--lumo-font-size-m)");

                var changedAgo = formatChangedAgoValue(fuelStation.getDetails().getLastchange());
                var changedTime = new Span("Last changed: " + changedAgo + " ago");

                var layoutNameAddress = new Div(name, address);
                layoutNameAddress.setHeightFull();
                layoutNameAddress.addClassName(LumoUtility.Gap.SMALL);
                layoutNameAddress.addClassName(LumoUtility.Padding.SMALL);
                layoutNameAddress.getStyle().setAlignItems(Style.AlignItems.START);
                layoutNameAddress.getStyle().setMarginRight("auto");

                var layoutPriceDistance = new Div(price, distance, changedTime);
                layoutPriceDistance.setHeightFull();
                layoutPriceDistance.addClassName(LumoUtility.Padding.XSMALL);
                layoutPriceDistance.setWidth("min-content");
                layoutPriceDistance.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
                layoutPriceDistance.getStyle().setAlignItems(Style.AlignItems.CENTER);
                price.getStyle().setAlignSelf(Style.AlignSelf.END);
                distance.getStyle().setMarginLeft("auto");

                var layoutRow = new Div(layoutNameAddress, layoutPriceDistance);
                layoutRow.addClassName("fuelstation-result");
                layoutRow.getStyle().setPadding("10px 15px");
                layoutRow.getStyle().setMargin("10px 0");
                layoutRow.addClassName("temp");
                layoutRow.setHeight("min-content");
                layoutRow.getStyle().setAlignItems(Style.AlignItems.CENTER);
                layoutRow.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
                layoutRow.getStyle().setBorder("3px solid var(--lumo-contrast-10pct)");

                fuelStationsLayout.add(layoutRow);
            });
        } else {
            var h2 = new H2("No fuel stations found for: " + currentLocation.getLatitude() + ", " +
                    currentLocation.getLongitude() + " in a radius of " + radiusField.getValue() + " km.");
            h2.addClassName("temp");
            fuelStationsLayout.add(h2);
        }
    }

    private String formatChangedAgoValue(LocalDateTime lastChange) {
        var changedAgo = lastChange.until(LocalDateTime.now(), ChronoUnit.MINUTES);

        if (changedAgo > 60) {
            var lastChangedInHours = lastChange.until(LocalDateTime.now(), ChronoUnit.HOURS);
            if (changedAgo % 60 > 30) {
                return lastChangedInHours + .5 + " hours";
            }
            return lastChangedInHours + " hours";
        } else {
            return changedAgo + " minutes";
        }
    }

    private <T extends Component> void removeComponentsByClassName(T parent, String className) {
        parent.getChildren()
                .filter(child -> child.getClassNames().stream().anyMatch(name -> name.equals(className)))
                .forEach(Component::removeFromParent);
    }

    private static class LazyComponent extends Div {
        public LazyComponent(SerializableSupplier<? extends Component> supplier) {
            addAttachListener(e -> {
                if (getElement().getChildCount() == 0) {
                    add(supplier.get());
                }
            });
        }
    }

    private void renderEfficiencyCalc() {
        efficiencyLayout.addClassName(HORIZONTAL_LAYOUT);
        removeComponentsByClassName(efficiencyLayout, EFFICIENCY_CALC);

        var consumption = new NumberField("Consumption", "6.5");
        consumption.setSuffixComponent(new Span("L/100Km"));
        consumption.addClassName(EFFICIENCY_CALC);

        var amountGas = new NumberField("Amount of Gas", "42.5");
        amountGas.setSuffixComponent(new Span("L"));
        amountGas.addClassName(EFFICIENCY_CALC);

        var button = new Button("Calculate", FontAwesome.Solid.CALCULATOR.create());
        button.addClassName(EFFICIENCY_CALC);
        button.getStyle().setPadding("10px");

        button.addClickListener(event -> {
            var resultsLayout = new Div(new H3("Results: "));
            resultsLayout.addClassName("efficiencyCalc_result");
            resultsLayout.getStyle().setMarginLeft("auto");
            var fuelStations = new LinkedHashMap<>(calculateEfficiency(consumption.getValue(), amountGas.getValue()));
            removeComponentsByClassName(efficiencyLayout, "efficiencyCalc_result");

            fuelStations.entrySet().stream().limit(3).forEach(entry -> {
                var fuelStation = entry.getKey();
                var price = entry.getValue();

                var bigDecimal = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
                var paragraph = new Paragraph(
                        new Paragraph(fuelStation.getName() + ", " + fuelStation.getAddress() + ", " + fuelStation.getPrice() + "€/L"),
                        new Text("Distance: " + fuelStation.getDistance() + "km, Total: " + bigDecimal + "€")
                );
                resultsLayout.add(paragraph);
            });
            efficiencyLayout.add(resultsLayout);
        });

        var optionsInputDiv = new Div(consumption, amountGas);
        optionsInputDiv.getStyle().setMarginBottom("10px");
        optionsInputDiv.addClassName(HORIZONTAL_LAYOUT);

        var optionsDiv = new Div(optionsInputDiv, button);
        optionsDiv.addClassName(EFFICIENCY_CALC);
        efficiencyLayout.addComponentAsFirst(optionsDiv);
    }

    private java.util.LinkedHashMap<FuelStation, Double> calculateEfficiency(double consumption, double amountGas) {
        var fuelStations = kraftstoffbilligerRequests
                .getFuelStationsByLocation(currentLocation, FuelType.fromName(fuelTypeSelect.getValue()), 5).stream()
                .filter(fuelStation -> fuelStation.getPrice() != 0.0)
                .toList();

        return efficiencyService.calculateMostEfficientFuelStation(consumption, amountGas, fuelStations);
    }
}
