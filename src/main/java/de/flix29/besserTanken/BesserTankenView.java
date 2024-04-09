package de.flix29.besserTanken;

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

    public BesserTankenView() {
        Select<String> fuelTypeSelect = new Select<>();
        fuelTypeSelect.setItems(Arrays.stream(FuelType.values())
                .map(FuelType::getName)
                .toArray(String[]::new));
        fuelTypeSelect.setLabel("Select fuel type: ");
        fuelTypeSelect.setValue(FuelType.DIESEL.getName());

        var radiusField = new TextField("Enter radius (km): ", "5", "5");
        var placeField = new TextField("Place or plz: ", "'Berlin' or '10178'");

        Select<String> orderBySelect = new Select<>(selectStringComponentValueChangeEvent ->
                performSearch(
                        placeField.getValue(),
                        FuelType.fromName(fuelTypeSelect.getValue()),
                        radiusField.getValue().isEmpty() ? null : Integer.parseInt(radiusField.getValue()),
                        selectStringComponentValueChangeEvent.getValue(),
                        10
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
                        orderBySelect.getValue(),
                        10
                )
        );

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

    private void performSearch(String place, FuelType fuelType, Integer radius, String orderBy, int limit) {
        List<FuelStation> fuelStationsByPlzOrPlace;
        if (!place.isEmpty()) {
            try {
                var plz = Integer.parseInt(place);
                LOGGER.info("Searching plz: {} with fuel type: {} and radius: {}.", plz, fuelType, radius);
                fuelStationsByPlzOrPlace = kraftstoffbilligerRequests.getFuelStationsByPlz(plz, fuelType, radius);
            } catch (NumberFormatException e) {
                LOGGER.info("Searching place: {} with fuel type: {} and radius: {}.", place, fuelType, radius);
                fuelStationsByPlzOrPlace = kraftstoffbilligerRequests.getFuelStationsByPlace(place, fuelType, radius);
            }
            LOGGER.info("Found {} fuel stations.", fuelStationsByPlzOrPlace.size());
            displayFuelStations(fuelStationsByPlzOrPlace, orderBy, limit);
        } else {
            LOGGER.warn("Please fill in a place or plz.");
        }
    }

    private void displayFuelStations(List<FuelStation> fuelStationsByPlzOrPlace, String orderBy, int limit) {
        getChildren()
                .filter(child -> child.hasClassName("temp"))
                .forEach(this::remove);

        if (fuelStationsByPlzOrPlace != null) {
            fuelStationsByPlzOrPlace.stream()
                    .filter(fuelStation -> fuelStation.getPrice() != 0.0)
                    .sorted((fuelStation1, fuelStation2) -> {
                        if (orderBy.equals("Distance")) {
                            return fuelStation1.getDistance().compareTo(fuelStation2.getDistance());
                        } else {
                            return Double.compare(fuelStation1.getPrice(), fuelStation2.getPrice());
                        }
                    })
                    .limit(limit)
                    .forEach(fuelStation -> {
                        //TODO better naming
                        var h1 = new H1(fuelStation.getPrice() + "€");
                        h1.setWidth("max-content");

                        var h3 = new H3(fuelStation.getName());
                        h3.setWidth("max-content");

                        var textMedium = new Paragraph(fuelStation.getAddress() + ", " + fuelStation.getCity());
                        textMedium.setWidthFull();
                        textMedium.getStyle().setFontSize("var(--lumo-font-size-m)");

                        var textMedium2 = new Paragraph(fuelStation.getDistance() + " km");
                        textMedium2.setWidth("max-content");
                        textMedium2.getStyle().setFontSize("var(--lumo-font-size-m)");

                        var layoutColumn2 = new VerticalLayout(h3, textMedium);
                        layoutColumn2.setHeightFull();
                        layoutColumn2.addClassName(LumoUtility.Gap.SMALL);
                        layoutColumn2.addClassName(LumoUtility.Padding.SMALL);
                        layoutColumn2.setWidthFull();
                        layoutColumn2.setJustifyContentMode(JustifyContentMode.CENTER);
                        layoutColumn2.setAlignItems(Alignment.START);

                        var layoutColumn3 = new VerticalLayout(h1, textMedium2);
                        layoutColumn3.setHeightFull();
                        layoutColumn3.setSpacing(false);
                        layoutColumn3.addClassName(LumoUtility.Padding.XSMALL);
                        layoutColumn3.setWidth("min-content");
                        layoutColumn3.setJustifyContentMode(JustifyContentMode.CENTER);
                        layoutColumn3.setAlignItems(Alignment.CENTER);
                        layoutColumn3.setAlignSelf(FlexComponent.Alignment.END, h1);
                        layoutColumn3.setAlignSelf(FlexComponent.Alignment.CENTER, textMedium2);

                        var layoutRow = new HorizontalLayout(layoutColumn2, layoutColumn3);
                        layoutRow.addClassName("temp");
                        layoutRow.setWidthFull();
                        layoutRow.setHeight("min-content");
                        layoutRow.setAlignItems(Alignment.CENTER);
                        layoutRow.setJustifyContentMode(JustifyContentMode.CENTER);
                        layoutRow.setFlexGrow(1.0, layoutColumn2);
                        layoutRow.setFlexGrow(1.0, layoutColumn3);
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
