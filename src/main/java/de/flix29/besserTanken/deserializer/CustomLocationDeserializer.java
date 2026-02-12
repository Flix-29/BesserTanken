package de.flix29.besserTanken.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;

import java.lang.reflect.Type;

public class CustomLocationDeserializer implements JsonDeserializer<SimpleLocation> {

    @Override
    public SimpleLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();

        return new SimpleLocation()
                .setLatitude(jsonObject.get("lat").getAsDouble())
                .setLongitude(jsonObject.get("lon").getAsDouble());
    }
}
