package de.flix29.besserTanken;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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

        var searchButton = new Button("Search",
                event -> performSearch(placeField.getValue(),
                        FuelType.fromName(fuelTypeSelect.getValue()),
                        radiusField.getValue().isEmpty() ? null : Integer.parseInt(radiusField.getValue())
                )
        );

        var horizontalLayout = new HorizontalLayout(
                placeField,
                fuelTypeSelect,
                radiusField,
                searchButton
        );
        horizontalLayout.setVerticalComponentAlignment(Alignment.END, searchButton);

        add(
                new H1("BesserTanken"),
                horizontalLayout
        );
    }

    private void performSearch(String place, FuelType fuelType, Integer radius) {
        List<FuelStation> fuelStationsByPlzOrPlace = null;
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
        } else {
            LOGGER.warn("Please fill in a place or plz.");
        }
        displayFuelStations(fuelStationsByPlzOrPlace);
    }

    private void displayFuelStations(List<FuelStation> fuelStationsByPlzOrPlace) {
        if (fuelStationsByPlzOrPlace != null) {
            fuelStationsByPlzOrPlace.stream()
                    .filter(fuelStation -> fuelStation.getPrice() != 0.0)
                    .forEach(fuelStation -> {
                H1 h1 = new H1(fuelStation.getPrice() + "€");
                h1.setWidth("max-content");

                H3 h3 = new H3(fuelStation.getName());
                h3.setWidth("max-content");

                Paragraph textMedium = new Paragraph(fuelStation.getAddress() + ", " + fuelStation.getCity());
                textMedium.setWidthFull();
                textMedium.getStyle().setFontSize("var(--lumo-font-size-m)");

                Paragraph textMedium2 = new Paragraph(fuelStation.getDistance() + " km");
                textMedium2.setWidth("max-content");
                textMedium2.getStyle().setFontSize("var(--lumo-font-size-m)");

                VerticalLayout layoutColumn2 = new VerticalLayout(h3, textMedium);
                layoutColumn2.setHeightFull();
                layoutColumn2.addClassName(LumoUtility.Gap.SMALL);
                layoutColumn2.addClassName(LumoUtility.Padding.SMALL);
                layoutColumn2.setWidthFull();
                layoutColumn2.setHeightFull();
                layoutColumn2.setJustifyContentMode(JustifyContentMode.CENTER);
                layoutColumn2.setAlignItems(Alignment.START);

                VerticalLayout layoutColumn3 = new VerticalLayout(h1, textMedium2);
                layoutColumn3.setHeightFull();
                layoutColumn3.setSpacing(false);
                layoutColumn3.addClassName(LumoUtility.Padding.XSMALL);
                layoutColumn3.setWidth("min-content");
                layoutColumn3.setHeightFull();
                layoutColumn3.setJustifyContentMode(JustifyContentMode.CENTER);
                layoutColumn3.setAlignItems(Alignment.CENTER);
                layoutColumn3.setAlignSelf(FlexComponent.Alignment.END, h1);
                layoutColumn3.setAlignSelf(FlexComponent.Alignment.CENTER, textMedium2);

                HorizontalLayout layoutRow = new HorizontalLayout(layoutColumn2, layoutColumn3);
                layoutRow.setWidthFull();
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
            add(new H2("No fuel stations found."));
        }
    }

}
