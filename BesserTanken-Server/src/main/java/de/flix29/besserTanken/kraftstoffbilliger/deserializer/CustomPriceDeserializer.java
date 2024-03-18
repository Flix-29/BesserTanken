package de.flix29.besserTanken.kraftstoffbilliger.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.flix29.besserTanken.kraftstoffbilliger.model.FuelType;
import de.flix29.besserTanken.kraftstoffbilliger.model.Price;

import java.lang.reflect.Type;
import java.util.List;

public class CustomPriceDeserializer extends CustomDeserializerUtils implements JsonDeserializer<List<Price>> {

    @Override
    public List<Price> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonObject();

        return List.of(
                new Price(FuelType.E5, getAsDoubleOrDefault(jsonObject,"1")),
                new Price(FuelType.E10, getAsDoubleOrDefault(jsonObject,"2")),
                new Price(FuelType.DIESEL, getAsDoubleOrDefault(jsonObject,"3")),
                new Price(FuelType.LPG, getAsDoubleOrDefault(jsonObject,"4")),
                new Price(FuelType.CNG, getAsDoubleOrDefault(jsonObject,"5"))
        );
    }

}
