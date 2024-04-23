package de.flix29.besserTanken;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.flix29.besserTanken.kraftstoffbilliger.KraftstoffbilligerRequests;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.tuple.Pair;
import org.gwtproject.geolocation.client.Callback;
import org.gwtproject.geolocation.client.Geolocation;
import org.gwtproject.geolocation.client.Position;
import org.gwtproject.geolocation.client.PositionError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@PageTitle("BesserTanken")
@Route(value = "besserTanken")
@RouteAlias(value = "")
@PermitAll
public class BesserTankenView extends VerticalLayout {

    private final Logger LOGGER = LoggerFactory.getLogger(BesserTankenView.class);
    private final KraftstoffbilligerRequests kraftstoffbilligerRequests = new KraftstoffbilligerRequests();

    private List<FuelStation> fuelStations;
    private Location currentLocation;

    public BesserTankenView() {
        currentLocation = getCurrentLocation();

        var radiusField = new TextField("Enter radius (km): ", "5", "5");
        var placeField = new TextField("Place or plz: ", "'Berlin' or '10178'");

        AtomicBoolean useCurrentLocation = new AtomicBoolean();
        Select<String> useCurrentLocationSelect = new Select<>(event -> {
            useCurrentLocation.set(event.getValue().equals("Use location"));
            placeField.setValue("");
            placeField.setVisible(!useCurrentLocation.get());
        });
        useCurrentLocationSelect.setItems("Use location", "Use plz/place");
        useCurrentLocationSelect.setLabel("Select search type: ");
        useCurrentLocationSelect.setValue("Use plz/place");

        Select<String> fuelTypeSelect = new Select<>();
        fuelTypeSelect.setItems(Arrays.stream(FuelType.values())
                .map(FuelType::getName)
                .toArray(String[]::new));
        fuelTypeSelect.setLabel("Select fuel type: ");
        fuelTypeSelect.setValue(FuelType.DIESEL.getName());

        Select<String> orderBySelect = new Select<>(event ->
                displayFuelStations(
                        event.getValue()
                )
        );
        orderBySelect.setItems("Price", "Distance");
        orderBySelect.setLabel("Order by: ");
        orderBySelect.setValue("Price");

        var searchButton = new Button("Search",
                event -> performSearch(
                        useCurrentLocation.get() ? currentLocation : null,
                        placeField.getValue(),
                        FuelType.fromName(fuelTypeSelect.getValue()),
                        radiusField.getValue().isEmpty() ? null : Integer.parseInt(radiusField.getValue()),
                        orderBySelect.getValue()
                )
        );
        searchButton.addClickShortcut(Key.ENTER);

        HorizontalLayout orderByLayout = new HorizontalLayout(orderBySelect);
        orderByLayout.setWidthFull();
        orderByLayout.setJustifyContentMode(JustifyContentMode.END);

        var horizontalLayout = new HorizontalLayout(
                useCurrentLocationSelect,
                placeField,
                fuelTypeSelect,
                radiusField,
                searchButton,
                orderByLayout
        );
        horizontalLayout.setWidthFull();
        horizontalLayout.setVerticalComponentAlignment(Alignment.END, searchButton);

        add(
                new H1("BesserTanken"),
                horizontalLayout,
                new Hr()
        );
    }

    private Location getCurrentLocation() {
//        List<ScriptEngineFactory> engineFactories = new ScriptEngineManager().getEngineFactories();
//        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
//        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("graal.js");

        AtomicReference<Location> currentPosition = new AtomicReference<>(new Location());
//
        var current = UI.getCurrent().getPage();

//        UI.getCurrent().getPage().addJavaScript("src/main/javascript/Geolocator.js");
//        UI.getCurrent().getPage().addJavaScript("frontend://geolocation.js");

        LOGGER.info("start");
        UI.getCurrent().getPage().executeJs("""
                function getCurrentLocation() {
                    return new Promise((resolve, reject) => {
                        navigator.geolocation.getCurrentPosition(
                            position => {
                                let coords = position.coords;
                                resolve([coords.latitude, coords.longitude]);
                            },
                            error => {
                                reject(error);
                            }
                        );
                    });
                }
                
                getCurrentLocation().then(
                    coords => $0.$server.receiveCoords(coords)
                ).catch(error => {
                    console.error("Error getting location:", error);
                    return null;
                });
                """, this);
//                .then(Double[].class, result -> {
//                    Location location = new Location();
//                    LOGGER.info("Result: {}, {}", result[0], result[1]);
//                    location.setCoords(Pair.of(result[0], result[1]));
//
//                    performSearch(location, "", FuelType.DIESEL, 5, "Price");
//                });

        //TODO use constructor


//        return currentPosition.get();



//        try {
//            scriptEngine.eval(Files.newBufferedReader(Path.of("src/main/javascript/Geolocator.js")));
//
//            Invocable invocable = (Invocable) scriptEngine;
//
//            currentPosition = (Double[]) invocable.invokeFunction("getCurrentPosition");
//        } catch (ScriptException | IOException | NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
//
//        if(currentPosition.get() == null) {
//            return null;
//        }
//
//        var location = new Location();
//        location.setCoords(Pair.of(currentPosition[0], currentPosition[1]));
//        return location;

//        Location location = new Location();
//        Geolocation geolocation = Geolocation.getIfSupported();
//
//        geolocation.getCurrentPosition(new Callback<>() {
//            @Override
//            public void onFailure(PositionError positionError) {
//            }
//
//            @Override
//            public void onSuccess(Position position) {
//                var latitude = position.getCoordinates().getLatitude();
//                var longitude = position.getCoordinates().getLongitude();
//                location.setCoords(Pair.of(latitude, longitude));
//                LOGGER.info("Use latitude: {}, Longitude: {}", latitude, longitude);
//            }
//        });
//
//        return location;
        return null;
    }

    private void receiveCoords(double latitude, double longitude) {
        Notification.show("Latitude: " + latitude + ", Longitude: " + longitude);
        // Now you can use latitude and longitude in your Java code
    }

    @ClientCallable
    private void receiveCoords(Double[] coords) {
        double latitude = coords[0];
        double longitude = coords[1];
        receiveCoords(latitude, longitude);
    }

    private void performSearch(Location location, String place, FuelType fuelType, Integer radius, String orderBy) {
        fuelStations = new ArrayList<>();
        if(location != null && location.getCoords() != null) {
            LOGGER.info("Searching location: {} with fuel type: {} and radius: {}.", location, fuelType, radius);
            fuelStations = kraftstoffbilligerRequests.getFuelStationsByLocation(List.of(location), fuelType, radius);
        } else if (!place.isEmpty()) {
            try {
                var plz = Integer.parseInt(place);
                LOGGER.info("Searching plz: {} with fuel type: {} and radius: {}.", plz, fuelType, radius);
                fuelStations = kraftstoffbilligerRequests.getFuelStationsByPlz(plz, fuelType, radius);
            } catch (NumberFormatException e) {
                LOGGER.info("Searching place: {} with fuel type: {} and radius: {}.", place, fuelType, radius);
                fuelStations = kraftstoffbilligerRequests.getFuelStationsByPlace(place, fuelType, radius);
            }
        } else {
            LOGGER.warn("Please fill in a place or plz or agree to use your location.");
        }
        LOGGER.info("Found {} fuel stations.", fuelStations.size());
        displayFuelStations(orderBy);
    }

    private void displayFuelStations(String orderBy) {
        getChildren()
                .filter(child -> child.hasClassName("temp"))
                .forEach(this::remove);

        if (fuelStations == null) return;

        if (!fuelStations.isEmpty()) {
            fuelStations.stream()
                    .filter(fuelStation -> fuelStation.getPrice() != 0.0)
                    .sorted((fuelStation1, fuelStation2) -> {
                        if (orderBy.equals("Distance")) {
                            return fuelStation1.getDistance().compareTo(fuelStation2.getDistance());
                        } else {
                            return Double.compare(fuelStation1.getPrice(), fuelStation2.getPrice());
                        }
                    })
                    .limit(10)
                    .forEach(fuelStation -> {
                        var price = new H1(fuelStation.getPrice() + "€");
                        price.setWidth("max-content");

                        var name = new H3(fuelStation.getName());
                        name.setWidth("max-content");

                        var address = new Paragraph(fuelStation.getAddress() + ", " + fuelStation.getCity());
                        address.setWidthFull();
                        address.getStyle().setFontSize("var(--lumo-font-size-m)");

                        var distance = new Paragraph(fuelStation.getDistance() + " km");
                        distance.setWidth("max-content");
                        distance.getStyle().setFontSize("var(--lumo-font-size-m)");

                        var layoutNameAddress = new VerticalLayout(name, address);
                        layoutNameAddress.setHeightFull();
                        layoutNameAddress.addClassName(LumoUtility.Gap.SMALL);
                        layoutNameAddress.addClassName(LumoUtility.Padding.SMALL);
                        layoutNameAddress.setWidthFull();
                        layoutNameAddress.setJustifyContentMode(JustifyContentMode.CENTER);
                        layoutNameAddress.setAlignItems(Alignment.START);

                        var layoutPriceDistance = new VerticalLayout(price, distance);
                        layoutPriceDistance.setHeightFull();
                        layoutPriceDistance.setSpacing(false);
                        layoutPriceDistance.addClassName(LumoUtility.Padding.XSMALL);
                        layoutPriceDistance.setWidth("min-content");
                        layoutPriceDistance.setJustifyContentMode(JustifyContentMode.CENTER);
                        layoutPriceDistance.setAlignItems(Alignment.CENTER);
                        layoutPriceDistance.setAlignSelf(Alignment.END, price);
                        layoutPriceDistance.setAlignSelf(Alignment.CENTER, distance);

                        var layoutRow = new HorizontalLayout(layoutNameAddress, layoutPriceDistance);
                        layoutRow.addClassName("temp");
                        layoutRow.setWidthFull();
                        layoutRow.setHeight("min-content");
                        layoutRow.setAlignItems(Alignment.CENTER);
                        layoutRow.setJustifyContentMode(JustifyContentMode.CENTER);
                        layoutRow.setFlexGrow(1.0, layoutNameAddress);
                        layoutRow.setFlexGrow(1.0, layoutPriceDistance);
                        layoutRow.getStyle().setBorder("3px solid var(--lumo-contrast-10pct)");

                        setWidthFull();
                        getStyle().set("flex-grow", "1");
                        setFlexGrow(1.0, layoutRow);
                        add(layoutRow);
                    });
        } else {
            var h2 = new H2("No fuel stations found.");
            h2.addClassName("temp");
            add(h2);
        }
    }

}
