package de.flix29.besserTanken.kraftstoffbilliger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static de.flix29.besserTanken.Constants.API_KEY;
import static de.flix29.besserTanken.model.Endpoint.*;

public class KraftstoffbilligerJob {

    public String getFuelTypes() throws IOException, InterruptedException {
        HttpRequest requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(TYPES_ENDPOINT.getUrl()))
                .header("apikey", API_KEY)
                .build();

        var response = HttpClient.newHttpClient().send(requestBuilder, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getFuelStations(String fuelType, double lat, double lon, String radius) throws IOException, InterruptedException {
        Map<String, String> formData = new HashMap<>();
        formData.put("type", fuelType);
        formData.put("lat", String.valueOf(lat));
        formData.put("lon", String.valueOf(lon));
        formData.put("radius", radius);

        HttpRequest requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(SEARCH_ENDPOINT.getUrl()))
                .header("apikey", API_KEY)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                .build();

        var response = HttpClient.newHttpClient().send(requestBuilder, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getFuelStationRoute(int lat, int lon, int lat2, int lon2, int type, String map) throws IOException, InterruptedException {
        Map<String, String> formData = new HashMap<>();
        formData.put("lat", String.valueOf(lat));
        formData.put("lon", String.valueOf(lon));
        formData.put("lat2", String.valueOf(lat2));
        formData.put("lon2", String.valueOf(lon2));
        formData.put("type", String.valueOf(type));
        formData.put("map", map);

        HttpRequest requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ROUTING_ENDPOINT.getUrl()))
                .header("apikey", API_KEY)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                .build();

        var response = HttpClient.newHttpClient().send(requestBuilder, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getFuelStationDetails(String id) throws IOException, InterruptedException {
        Map<String, String> formData = new HashMap<>();
        formData.put("id", id);

        HttpRequest requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(DETAILS_ENDPOINT.getUrl()))
                .header("apikey", API_KEY)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                .build();

        var response = HttpClient.newHttpClient().send(requestBuilder, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (!formBodyBuilder.isEmpty()) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }
}
