package de.flix29.besserTanken.model.kraftstoffbilliger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    private FuelType type;
    private double price;

}
