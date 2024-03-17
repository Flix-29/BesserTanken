package de.flix29.besserTanken.kraftstoffbilliger.deserializer;

import com.google.gson.reflect.TypeToken;
import de.flix29.besserTanken.model.FuelType;
import de.flix29.besserTanken.model.OpeningTime;
import de.flix29.besserTanken.model.Price;

import java.lang.reflect.Type;
import java.util.List;

public class CustomModelTypes {

    public static final Type FUEL_TYPE_LIST = new TypeToken<List<FuelType>>(){}.getType();
    public static final Type PRICE_LIST = new TypeToken<List<Price>>(){}.getType();
    public static final Type OPENING_TIMES_LIST = new TypeToken<List<OpeningTime>>(){}.getType();

}
