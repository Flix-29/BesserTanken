package de.flix29.besserTanken.deserializer;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class CustomDeserializerUtils {

    protected String getAsStringOrNull(JsonObject jsonObject, String key) {
        return jsonObject != null &&
                jsonObject.has(key) &&
                !(jsonObject.get(key) instanceof JsonNull) ? jsonObject.get(key).getAsString() : null;
    }

    protected double getAsDoubleOrDefault(JsonObject jsonObject, String key) {
        return jsonObject != null &&
                jsonObject.has(key) &&
                !(jsonObject.get(key) instanceof JsonNull) &&
                !jsonObject.get(key).getAsString().isEmpty() ? jsonObject.get(key).getAsDouble() : 0.0;
    }

}
