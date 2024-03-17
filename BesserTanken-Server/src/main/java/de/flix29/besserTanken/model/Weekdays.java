package de.flix29.besserTanken.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Weekdays {

    MONDAY("Monday", "mo", 1),
    TUESDAY("Tuesday", "tu", 2),
    WEDNESDAY("Wednesday", "we", 3),
    THURSDAY("Thursday", "th", 4),
    FRIDAY("Friday", "fr", 5),
    SATURDAY("Saturday", "sa", 6),
    SUNDAY("Sunday", "su", 7);

    private final String name;
    private final String shortName;
    private final int number;

    public static Weekdays fromName(String name) {
        for (Weekdays weekday : Weekdays.values()) {
            if (weekday.getName().equals(name)) {
                return weekday;
            }
        }
        return null;
    }

    public static Weekdays fromShortName(String shortName) {
        for (Weekdays weekday : Weekdays.values()) {
            if (weekday.getShortName().equals(shortName)) {
                return weekday;
            }
        }
        return null;
    }

    public static Weekdays fromNumber(int number) {
        for (Weekdays weekday : Weekdays.values()) {
            if (weekday.getNumber() == number) {
                return weekday;
            }
        }
        return null;
    }

}
