package de.flix29.besserTanken.kraftstoffbilliger.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.flix29.besserTanken.model.OpeningTime;
import de.flix29.besserTanken.model.Weekdays;

import java.lang.reflect.Type;
import java.util.List;

public class CustomOpeningTimeDeserializer extends CustomDeserializerUtils implements JsonDeserializer<List<OpeningTime>> {
    @Override
    public List<OpeningTime> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var jsonObject = jsonElement.getAsJsonObject();

        return List.of(
                new OpeningTime(Weekdays.MONDAY, jsonObject.get("mo").getAsString()),
                new OpeningTime(Weekdays.TUESDAY, jsonObject.get("tu").getAsString()),
                new OpeningTime(Weekdays.WEDNESDAY, jsonObject.get("we").getAsString()),
                new OpeningTime(Weekdays.THURSDAY, jsonObject.get("th").getAsString()),
                new OpeningTime(Weekdays.FRIDAY, jsonObject.get("fr").getAsString()),
                new OpeningTime(Weekdays.SATURDAY, jsonObject.get("sa").getAsString()),
                new OpeningTime(Weekdays.SUNDAY, jsonObject.get("su").getAsString())
        );
    }
}
