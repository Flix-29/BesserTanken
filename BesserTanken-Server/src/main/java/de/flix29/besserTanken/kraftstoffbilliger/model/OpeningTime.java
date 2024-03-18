package de.flix29.besserTanken.kraftstoffbilliger.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpeningTime {

    private Weekdays day;
    private String times;

}
