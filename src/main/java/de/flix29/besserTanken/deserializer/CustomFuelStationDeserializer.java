package de.flix29.besserTanken.deserializer;

import com.google.gson.*;
import de.flix29.besserTanken.model.kraftstoffbilliger.FuelStation;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomFuelStationDeserializer extends CustomDeserializerUtils implements JsonDeserializer<FuelStation> {
    @Override
    public FuelStation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonObject();
        var fuelStation = new FuelStation();
        fuelStation.setId(getAsStringOrNull(jsonObject, "id"));
        fuelStation.setBrand(getAsStringOrNull(jsonObject, "brand"));
        fuelStation.setName(getAsStringOrNull(jsonObject, "name"));
        fuelStation.setAddress(getAsStringOrNull(jsonObject, "address"));
        fuelStation.setCity(getAsStringOrNull(jsonObject, "city"));
        fuelStation.setDistance(BigDecimal.valueOf(getAsDoubleOrDefault(jsonObject, "distance")).setScale(2, RoundingMode.HALF_UP));
        fuelStation.setLat(getAsStringOrNull(jsonObject, "lat"));
        fuelStation.setLon(getAsStringOrNull(jsonObject, "lon"));
        fuelStation.setPrice(getAsDoubleOrDefault(jsonObject, "price"));
        return fuelStation;
    }

}
