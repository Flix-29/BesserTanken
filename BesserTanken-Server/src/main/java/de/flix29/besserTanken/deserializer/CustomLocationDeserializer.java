package de.flix29.besserTanken.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.flix29.besserTanken.model.openDataSoft.Location;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;

public class CustomLocationDeserializer implements JsonDeserializer<Location> {


    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var location = new Location();
        var jsonObject = jsonElement.getAsJsonObject();
        location.setPlz(jsonObject.get("name").getAsInt());
        location.setName(jsonObject.get("plz_name").getAsString());
        var coords = jsonObject.get("geo_point_2d").getAsJsonObject();
        location.setCoords(Pair.of(coords.get("lat").getAsDouble(), coords.get("lon").getAsDouble()));
        return location;
    }
}
