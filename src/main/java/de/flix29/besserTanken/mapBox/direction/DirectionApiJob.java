package de.flix29.besserTanken.mapBox.direction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.flix29.BesserTanken;
import de.flix29.besserTanken.deserializer.CustomRouteDeserializer;
import de.flix29.besserTanken.model.mapBox.Route;
import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

import static de.flix29.besserTanken.deserializer.CustomModelTypes.ROUTE_LIST_TYPE;

@Service
public class DirectionApiJob {

    private final Logger LOGGER = LoggerFactory.getLogger(DirectionApiJob.class);
    private final String baseUrl = "https://api.mapbox.com/directions/v5/mapbox/driving-traffic/$startLon$%2C" +
            "$startLat$%3B$endLon$%2C$endLat$?alternatives=true&geometries=geojson&language=en&overview=full&steps=true&access_token=$apiKey$";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Route.class, new CustomRouteDeserializer())
            .setPrettyPrinting()
            .create();


    private String buildUrl(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        final var apiKey = BesserTanken.getSecrets().getOrDefault("directionsKey", "");
        return baseUrl
                .replace("$startLat$", String.valueOf(startLatitude))
                .replace("$startLon$", String.valueOf(startLongitude))
                .replace("$endLat$", String.valueOf(endLatitude))
                .replace("$endLon$", String.valueOf(endLongitude))
                .replace("$apiKey$", apiKey);
    }

    public <T extends SimpleLocation> List<Route> getRoutes(T start, T end) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude())))
                .GET();

        var response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        try {
            var jsonArray = gson.fromJson(response.body(), JsonObject.class).get("routes").getAsJsonArray();
            return gson.fromJson(jsonArray, ROUTE_LIST_TYPE);
        } catch (Exception e) {
            LOGGER.error("Error while parsing JSON: {}", response.body(), e);
            return Collections.emptyList();
        }
    }
}
