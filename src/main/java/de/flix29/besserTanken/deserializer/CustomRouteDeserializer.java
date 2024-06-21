package de.flix29.besserTanken.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.flix29.besserTanken.model.mapBox.Route;
import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CustomRouteDeserializer extends CustomDeserializerUtils implements JsonDeserializer<Route> {
    @Override
    public Route deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonObject();
        var route = new Route();
        route.setWeightTypical(getAsDoubleOrDefault(jsonObject, "weight_typical"));
        route.setDurationTypical(getAsDoubleOrDefault(jsonObject, "duration_typical"));
        route.setWeightName(getAsStringOrNull(jsonObject, "weight_name"));
        route.setWeight(getAsDoubleOrDefault(jsonObject, "weight"));
        route.setDuration(getAsDoubleOrDefault(jsonObject, "duration"));
        route.setDistance(getAsDoubleOrDefault(jsonObject, "distance"));
        route.setLegs(null); //Not implemented
        route.setGeometry(new ArrayList<>());

        jsonObject.get("geometry").getAsJsonObject().get("coordinates").getAsJsonArray().forEach(jsonElement1 -> {
            var location = jsonElement1.getAsJsonArray();
            var latitude = location.get(1).getAsDouble();
            var longitude = location.get(0).getAsDouble();

            route.getGeometry().add(new SimpleLocation(latitude, longitude));
        });

        return route;
    }

}
