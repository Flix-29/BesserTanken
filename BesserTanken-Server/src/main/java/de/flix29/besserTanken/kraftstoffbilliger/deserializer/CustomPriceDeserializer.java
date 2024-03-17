package de.flix29.besserTanken.kraftstoffbilliger.deserializer;

import com.google.gson.*;
import de.flix29.besserTanken.model.FuelType;
import de.flix29.besserTanken.model.Price;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class CustomPriceDeserializer implements JsonDeserializer<List<Price>> {

    @Override
    public List<Price> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonObject();

        return List.of(
                new Price(FuelType.E5, getAsDoubleOrDefault("1", jsonObject)),
                new Price(FuelType.E10, getAsDoubleOrDefault("2", jsonObject)),
                new Price(FuelType.DIESEL, getAsDoubleOrDefault("3", jsonObject)),
                new Price(FuelType.LPG, getAsDoubleOrDefault("4", jsonObject)),
                new Price(FuelType.CNG, getAsDoubleOrDefault("5", jsonObject))
        );
    }

    private double getAsDoubleOrDefault(String key, JsonObject jsonObject) {
        return jsonObject.has(key) &&
                jsonObject.get(key) != null &&
                !Objects.equals(jsonObject.get(key).getAsString(), "")
                ? jsonObject.get(key).getAsDouble() : 0.0;
    }
}
