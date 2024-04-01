package de.flix29.besserTanken.model.kraftstoffbilliger;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FuelStation implements Comparable<FuelStation> {

    private String id;
    private String brand;
    private String name;
    private String address;
    private String city;
    private BigDecimal distance;
    private String lat;
    private String lon;
    private double price;

    @Override
    public int compareTo(FuelStation o) {
        return Double.compare(this.getPrice(), o.getPrice());
    }
}
