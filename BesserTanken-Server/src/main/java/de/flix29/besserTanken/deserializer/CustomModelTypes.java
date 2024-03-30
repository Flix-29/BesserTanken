package de.flix29.besserTanken.deserializer;

import com.google.gson.reflect.TypeToken;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelType;
import de.flix29.besserTanken.model.kraftstoffbilliger.OpeningTime;
import de.flix29.besserTanken.model.kraftstoffbilliger.Price;
import de.flix29.besserTanken.model.openDataSoft.Location;

import java.lang.reflect.Type;
import java.util.List;

public class CustomModelTypes {

    public static final Type FUEL_TYPE_LIST_TYPE = new TypeToken<List<FuelType>>(){}.getType();
    public static final Type PRICE_LIST_TYPE = new TypeToken<List<Price>>(){}.getType();
    public static final Type FUEL_STATION_LIST_TYPE = new TypeToken<List<FuelStation>>(){}.getType();
    public static final Type OPENING_TIMES_LIST_TYPE = new TypeToken<List<OpeningTime>>(){}.getType();
    public static final Type LOCATION_TYPE = new TypeToken<List<Location>>(){}.getType();

}
