package de.flix29.besserTanken;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.TextStyle;
import com.vaadin.flow.component.map.events.MapFeatureClickEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.tabs.TabVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style.Position;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.flix29.BesserTanken;
import de.flix29.besserTanken.kraftstoffbilliger.KraftstoffbilligerRequests;
import de.flix29.besserTanken.mapBox.direction.DirectionApiRequests;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStationDetail;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.openDataSoft.Location;
import de.flix29.besserTanken.openDataSoft.OpenDataSoftRequests;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@PageTitle("BesserTanken")
@Route(value = "")
@PermitAll
public class BesserTankenView extends VerticalLayout {

    private final Logger LOGGER = LoggerFactory.getLogger(BesserTankenView.class);
    private final KraftstoffbilligerRequests kraftstoffbilligerRequests;
    private final OpenDataSoftRequests openDataSoftRequests;
    private final DirectionApiRequests directionApiRequests;

    private final VerticalLayout fuelStationsLayout = new VerticalLayout();
    private final VerticalLayout mapComponent = new VerticalLayout();

    private List<FuelStation> foundFuelStations;
    private List<FuelStation> displayedFuelStations;
    private boolean useCurrentLocation;
    private List<Location> currentLocation;

    private Map map;
    private final NumberField radiusField;
    private final Select<String> useCurrentLocationSelect;
    private final Select<String> orderBySelect;
    private final Select<String> resultLimitSelect;
    private final TabSheet tabSheet;


    public BesserTankenView(KraftstoffbilligerRequests kraftstoffbilligerRequests, OpenDataSoftRequests openDataSoftRequests, DirectionApiRequests directionApiRequests) {
        this.kraftstoffbilligerRequests = kraftstoffbilligerRequests;
        this.openDataSoftRequests = openDataSoftRequests;
        this.directionApiRequests = directionApiRequests;

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
        useCurrentLocationSelect.setItems("Use location", "Use plz/place");
        useCurrentLocationSelect.setLabel("Select search type: ");
        useCurrentLocationSelect.setValue("Use plz/place");

        Select<String> fuelTypeSelect = new Select<>();
        fuelTypeSelect.setItems(Arrays.stream(FuelType.values())
                .map(FuelType::getName)
                .toArray(String[]::new));
        fuelTypeSelect.setLabel("Select fuel type: ");
        fuelTypeSelect.setValue(FuelType.DIESEL.getName());

        resultLimitSelect = new Select<>(event -> displayFuelStations());
        resultLimitSelect.setItems("10", "25", "50", "all");
        resultLimitSelect.setLabel("Result limit:");
        resultLimitSelect.setValue("10");

        orderBySelect = new Select<>(event -> displayFuelStations());
        orderBySelect.setItems("Price", "Distance");
        orderBySelect.setLabel("Order by: ");
        orderBySelect.setValue("Price");

        var searchButton = new Button("Search", event -> {
            foundFuelStations = performSearch(
                    placeField.getValue(),
                    FuelType.fromName(fuelTypeSelect.getValue()),
                    radiusField.getValue() == null ? 0 : (int) Math.round(radiusField.getValue())
            );
            displayFuelStations();
        });
        searchButton.addClickShortcut(Key.ENTER);

        HorizontalLayout orderByLimitLayout = new HorizontalLayout(resultLimitSelect, orderBySelect);
        orderByLimitLayout.setWidthFull();
        orderByLimitLayout.setJustifyContentMode(JustifyContentMode.END);

        var horizontalLayout = new HorizontalLayout(
                useCurrentLocationSelect,
                placeField,
                fuelTypeSelect,
                radiusField,
                searchButton,
                orderByLimitLayout
        );
        horizontalLayout.setWidthFull();
        horizontalLayout.setVerticalComponentAlignment(Alignment.END, searchButton);

        var tab1 = new Tab(FontAwesome.Solid.GAS_PUMP.create(), new Span("Fuel Stations"));
        tab1.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab1.addClassName("FuelStations");
        var tab2 = new Tab(FontAwesome.Regular.MAP.create(), new Span("Map"));
        tab2.addThemeVariants(TabVariant.LUMO_ICON_ON_TOP);
        tab2.setClassName("Map");

        tabSheet = new TabSheet();
        tabSheet.add(tab1, fuelStationsLayout);
        tabSheet.add(tab2, new LazyComponent(() -> mapComponent));
        tabSheet.setWidthFull();
        tabSheet.addThemeVariants(TabSheetVariant.LUMO_BORDERED);
        tabSheet.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(tab1)) {
                removeComponentByClassName(this, "tooltip");
            } else if (event.getSelectedTab().equals(tab2)) {
                renderMap();
            }
        });

        var version = new Span(BesserTanken.getEnv().getProperty("bessertanken.version"));
        version.getStyle().set("padding-bottom", "3px");
        version.getStyle().setColor("var(--lumo-contrast-70pct)");
        var header = new HorizontalLayout(new H1("BesserTanken"), version);
        header.setAlignItems(Alignment.END);

        add(
                header,
                horizontalLayout,
                new Hr(),
                tabSheet
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
    @SuppressWarnings("unused")
    private void receiveCoords(Double[] coords) {
        if (coords == null || coords.length != 2) {
            LOGGER.warn("Received invalid coordinates.");
            currentLocation = null;
            useCurrentLocation = false;
            useCurrentLocationSelect.setValue("Use plz/place");
            return;
        }

        var location = new Location();
        location.setLatitude(coords[0]);
        location.setLongitude(coords[1]);
        currentLocation = List.of(location);

        if (tabSheet != null && tabSheet.getSelectedTab().getClassName().equals("Map")) {
            renderMap();
        }
    }

    private List<FuelStation> performSearch(String place, FuelType fuelType, Integer radius) {
        foundFuelStations = new ArrayList<>();
        if (!place.isEmpty()) {
            try {
                var plz = Integer.parseInt(place);
                LOGGER.info("Searching coords for plz: {}", plz);
                currentLocation = openDataSoftRequests.getCoordsFromPlz(plz);
            } catch (NumberFormatException e) {
                LOGGER.info("Searching coords for place: {}", place);
                currentLocation = openDataSoftRequests.getCoordsFromPlzName(place);
            }
        }

        if (currentLocation.isEmpty()) {
            LOGGER.warn("Please fill in a place or plz or agree to use your location.");
        }

        LOGGER.info("Searching location: {} with fuel type: {} and radius: {}.", currentLocation.toString(), fuelType, radius);
        foundFuelStations = kraftstoffbilligerRequests.getFuelStationsByLocation(currentLocation, fuelType, radius);
        LOGGER.info("Found {} fuel stations.", foundFuelStations.size());

        return foundFuelStations;
    }

    private void displayFuelStations() {
        removeComponentByClassName(fuelStationsLayout, "temp");

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

            displayedFuelStations.forEach(fuelStation -> {
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
                fuelStationsLayout.add(layoutRow);
            });

            if (tabSheet.getSelectedTab().getClassName().contains("Map")) {
                renderMap();
            }
        } else {
            var location = currentLocation.get(0);
            var h2 = new H2("No fuel stations found for: " + location.getLatitude() + ", " +
                    location.getLongitude() + " in a radius of " + radiusField.getValue() + " km.");
            h2.addClassName("temp");
            fuelStationsLayout.add(h2);
        }
    }

    private void renderMap() {
        mapComponent.removeAll();
        map = new Map();

//        XYZSource.Options sourceOptions = new XYZSource.Options();
//        // set the URL pattern for the map service containing x, y, and z
//        // parameters
//        // mapbox requires an access token, register on
//        // mapbox.com to get one, and place it in the line below
//        sourceOptions.setUrl(
//                "https://api.mapbox.com/styles/v1/mapbox/standard-v12/{z}/{x}/{y}.jpg90?access_token=pk.eyJ1IjoiZmxpeDI5IiwiYSI6ImNsd3F2azkwNTA2N28yaXF2cGtuNGk4ZzkifQ.baTjWHcFg5o06ebH1g3lxQ");
//        // using a map service usually requires setting
//        // attributions with copyright notices
//        sourceOptions.setAttributions(List.of(
//                "<a href=\"https://www.mapbox.com/about/maps/\">© Mapbox</a>",
//                "<a href=\"https://www.openstreetmap.org/about/\">© OpenStreetMap</a>"));
//        sourceOptions.setAttributionsCollapsible(false);
//        XYZSource source = new XYZSource(sourceOptions);
//        TileLayer tileLayer = new TileLayer();
//        tileLayer.setSource(source);
//        map.setBackgroundLayer(tileLayer);

        map.setHeight("800px");
        map.setZoom(13);

        map.addViewMoveEndEventListener(event -> {
            var textStyle = new TextStyle();
            if (event.getZoom() < 12.5) {
                textStyle.setScale(0);
            } else {
                textStyle.setScale(1);
            }
            map.getFeatureLayer().getFeatures().stream()
                    .filter(feature -> feature instanceof MarkerFeature)
                    .map(feature -> (MarkerFeature) feature)
                    .forEach(marker -> marker.setTextStyle(textStyle));
        });

        var startCoords = new Coordinate(13.4, 52.5);
        if (currentLocation != null && !currentLocation.isEmpty()) {
            var location = currentLocation.get(0);
            startCoords = new Coordinate(location.getLongitude(), location.getLatitude());
        }

        var locationMarker = new MarkerFeature(startCoords, getRedIcon());
        locationMarker.setText("Your location");
        locationMarker.setDraggable(false);

        map.getFeatureLayer().addFeature(locationMarker);
        map.setCenter(startCoords);

        if (displayedFuelStations == null) {
            mapComponent.add(map);
            return;
        }

        displayedFuelStations = kraftstoffbilligerRequests.addDetailsToFuelStations(displayedFuelStations);
        displayedFuelStations.forEach(fuelStation -> {
            var coords = new Coordinate(fuelStation.getDetails().getLon(), fuelStation.getDetails().getLat());
            var marker = new MarkerFeature(coords, getBlueIcon());
            marker.setText(fuelStation.getName());
            marker.setDraggable(false);

            map.getFeatureLayer().addFeature(marker);
        });

        map.addClickEventListener(event -> removeComponentByClassName(this, "tooltip"));
        map.addFeatureClickListener(event -> {
            var marker = (MarkerFeature) event.getFeature();
            var coordinates = marker.getCoordinates();
            var fuelStation = displayedFuelStations.stream()
                    .filter(fuelStationsItem -> fuelStationsItem.getDetails().getLat() == coordinates.getY() &&
                            fuelStationsItem.getDetails().getLon() == coordinates.getX())
                    .findFirst();

            removeComponentByClassName(this, "tooltip");
            var tooltip = getTooltip(event, fuelStation);

            add(tooltip);
        });

        mapComponent.add(map);
    }

    private Icon getRedIcon() {
        var optionsRed = new Icon.Options();
        optionsRed.setImg(new StreamResource("locationdot-lightcoral-duotone.png", () -> getClass().getResourceAsStream("/images/small_locationdot-lightcoral-duotone.png")));
        optionsRed.setAnchor(new Icon.Anchor(0.5, 0.8));
        return new Icon(optionsRed);
    }

    private Icon getBlueIcon() {
        var optionsBlue = new Icon.Options();
        optionsBlue.setImg(new StreamResource("locationdot-cornflowerblue-duotone.png", () -> getClass().getResourceAsStream("/images/small_locationdot-cornflowerblue-duotone.png")));
        optionsBlue.setAnchor(new Icon.Anchor(0.5, 0.8));
        return new Icon(optionsBlue);
    }

    private Div getTooltip(MapFeatureClickEvent event, Optional<FuelStation> fuelStation) {
        Div tooltip = new Div();
        tooltip.addClassName("tooltip");

        fuelStation.ifPresent(fuelStationItem -> {
            FuelStationDetail details = fuelStationItem.getDetails();

            tooltip.getStyle().setPosition(Position.ABSOLUTE);
            tooltip.getStyle().setBackgroundColor("var(--lumo-base-color)");
            tooltip.getStyle().setBorder("2px solid black");
            tooltip.getStyle().setBorderRadius("10px");
            tooltip.getStyle().setPadding("5px");

            tooltip.add(new H3(fuelStationItem.getName()));
            tooltip.add(new Paragraph(details.getAddress() + ", " + details.getCity()));
            tooltip.add(new Paragraph("Price: " + fuelStationItem.getPrice() + "€"));
            tooltip.add(new Paragraph("Distance: " + fuelStationItem.getDistance() + " km"));

            double x = event.getMouseDetails().getAbsoluteX();
            double y = event.getMouseDetails().getAbsoluteY();
            tooltip.getStyle().set("left", x + "px");
            tooltip.getStyle().set("top", y + "px");
        });
        return tooltip;
    }

    private <T extends Component> void removeComponentByClassName(T parent, String className) {
        parent.getChildren()
                .filter(child -> child.hasClassName(className))
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
}
