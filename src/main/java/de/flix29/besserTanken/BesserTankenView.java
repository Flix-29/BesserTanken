package de.flix29.besserTanken;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.flix29.besserTanken.kraftstoffbilliger.KraftstoffbilligerRequests;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import jakarta.annotation.security.PermitAll;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PageTitle("BesserTanken")
@Route(value = "")
@PermitAll
public class BesserTankenView extends VerticalLayout {

    private final Logger LOGGER = LoggerFactory.getLogger(BesserTankenView.class);
    private final KraftstoffbilligerRequests kraftstoffbilligerRequests = new KraftstoffbilligerRequests();

    protected final TextField radiusField;
    protected final TextField placeField;
    protected final Select<String> useCurrentLocationSelect;
    protected final Select<String> fuelTypeSelect;
    protected final Select<String> orderBySelect;
    protected final Button searchButton;

    private List<FuelStation> fuelStations;
    private boolean useCurrentLocation;
    private Location currentLocation;

    public BesserTankenView() {
        radiusField = new TextField("Enter radius (km): ", "5", "5");
        placeField = new TextField("Place or plz: ", "'Berlin' or '10178'");

        useCurrentLocationSelect = new Select<>(event -> {
            useCurrentLocation = event.getValue().equals("Use location");
            placeField.setValue("");
            placeField.setVisible(!useCurrentLocation);
            if(useCurrentLocation) {
                getCurrentLocation();
            } else {
                currentLocation = null;
            }
        });
        useCurrentLocationSelect.setItems("Use location", "Use plz/place");
        useCurrentLocationSelect.setLabel("Select search type: ");
        useCurrentLocationSelect.setValue("Use plz/place");

        fuelTypeSelect = new Select<>();
        fuelTypeSelect.setItems(Arrays.stream(FuelType.values())
                .map(FuelType::getName)
                .toArray(String[]::new));
        fuelTypeSelect.setLabel("Select fuel type: ");
        fuelTypeSelect.setValue(FuelType.DIESEL.getName());

        orderBySelect = new Select<>(event ->
                displayFuelStations(
                        event.getValue()
                )
        );
        orderBySelect.setItems("Price", "Distance");
        orderBySelect.setLabel("Order by: ");
        orderBySelect.setValue("Price");

        searchButton = new Button("Search",
                event -> performSearch(
                        useCurrentLocation ? currentLocation : null,
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

    private void getCurrentLocation() {
        LOGGER.info("Trying to get current location.");
        try {
            String javascript = Files.readString(Path.of("src/main/javascript/Geolocator.js"));
            UI.getCurrent().getPage().executeJs(javascript, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ClientCallable
    private void receiveCoords(Double[] coords) {
        if (coords == null || coords.length != 2) {
            LOGGER.warn("Received invalid coordinates.");
            currentLocation = null;
            useCurrentLocation = false;
            useCurrentLocationSelect.setValue("Use plz/place");
            return;
        }

        currentLocation = new Location();
        currentLocation.setCoords(Pair.of(coords[0], coords[1]));
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
            var h2 = new H2("No fuel stations found for your input.");
            h2.addClassName("temp");
            add(h2);
        }
    }

}
