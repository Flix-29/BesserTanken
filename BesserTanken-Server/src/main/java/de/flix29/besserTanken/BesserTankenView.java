package de.flix29.besserTanken;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import jakarta.annotation.security.PermitAll;

@PageTitle("BesserTanken")
@Route(value = "besserTanken")
@RouteAlias(value = "")
@PermitAll
public class BesserTankenView extends VerticalLayout {

    public BesserTankenView() {
        Select<FuelType> fuelTypeSelect = new Select<>();
        fuelTypeSelect.setLabel("Select fuel type: ");
        Button search = new Button("Search");
        add(
                new H1("BesserTanken"),
                new HorizontalLayout(
                        new TextField("Enter your location (place or plz): "),
                        fuelTypeSelect,
                        new TextField("Enter radius: "),
                        search
                )
        );
    }

}
