package de.flix29.besserTanken.mapBox.direction;

import de.flix29.BesserTanken;
import de.flix29.besserTanken.model.openDataSoft.Location;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DirectionApiRequests {

    private final String baseUrl = "https://api.mapbox.com/directions/v5/mapbox/driving-traffic/$startLatitude$%2C" +
            "$startLongitude$%3B$endLatitude$%2C$endLongitude$?alternatives=true&geometries=geojson&language=en&overview=full&steps=true&access_token=$apiKey$";

    private String buildUrl(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        final String apiKey = System.getenv().getOrDefault("DIRECTIONS_KEY", BesserTanken.getEnv().getProperty("directions.apikey"));
        return baseUrl
                .replace("$startLatitude$", String.valueOf(startLatitude))
                .replace("$startLongitude$", String.valueOf(startLongitude))
                .replace("$endLatitude$", String.valueOf(endLatitude))
                .replace("$endLongitude$", String.valueOf(endLongitude))
                .replace("$apiKey$", apiKey);
    }

    public double getRealDistance(Location start, Location end) {
        var route = getRoute(start, end);

        return 0.0;

    }

    public String getRoute(Location start, Location end) {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude())))
                .GET();

        try {
            return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
