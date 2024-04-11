package de.flix29.besserTanken;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.flix29.besserTanken.kraftstoffbilliger.KraftstoffbilligerRequests;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@PageTitle("BesserTanken")
@Route(value = "besserTanken")
@RouteAlias(value = "")
@PermitAll
public class BesserTankenView extends VerticalLayout {

    private final Logger LOGGER = LoggerFactory.getLogger(BesserTankenView.class);
    private final KraftstoffbilligerRequests kraftstoffbilligerRequests = new KraftstoffbilligerRequests();

    private List<FuelStation> fuelStations;

    public BesserTankenView() {
        Select<String> fuelTypeSelect = new Select<>();
        fuelTypeSelect.setItems(Arrays.stream(FuelType.values())
                .map(FuelType::getName)
                .toArray(String[]::new));
        fuelTypeSelect.setLabel("Select fuel type: ");
        fuelTypeSelect.setValue(FuelType.DIESEL.getName());

        var radiusField = new TextField("Enter radius (km): ", "5", "5");
        var placeField = new TextField("Place or plz: ", "'Berlin' or '10178'");

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

    private void performSearch(String place, FuelType fuelType, Integer radius, String orderBy) {
        if (!place.isEmpty()) {
            try {
                var plz = Integer.parseInt(place);
                LOGGER.info("Searching plz: {} with fuel type: {} and radius: {}.", plz, fuelType, radius);
                fuelStations = kraftstoffbilligerRequests.getFuelStationsByPlz(plz, fuelType, radius);
            } catch (NumberFormatException e) {
                LOGGER.info("Searching place: {} with fuel type: {} and radius: {}.", place, fuelType, radius);
                fuelStations = kraftstoffbilligerRequests.getFuelStationsByPlace(place, fuelType, radius);
            }
            LOGGER.info("Found {} fuel stations.", fuelStations.size());
            displayFuelStations(orderBy);
        } else {
            LOGGER.warn("Please fill in a place or plz.");
        }
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
                        layoutPriceDistance.setAlignSelf(FlexComponent.Alignment.END, price);
                        layoutPriceDistance.setAlignSelf(FlexComponent.Alignment.CENTER, distance);

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
