package de.flix29.besserTanken.kraftstoffbilliger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.flix29.besserTanken.deserializer.*;
import de.flix29.besserTanken.kraftstoffbilliger.model.FuelStation;
import de.flix29.besserTanken.kraftstoffbilliger.model.FuelStationDetail;
import de.flix29.besserTanken.kraftstoffbilliger.model.FuelType;
import de.flix29.besserTanken.kraftstoffbilliger.model.requests.Endpoints;
import de.flix29.besserTanken.kraftstoffbilliger.model.requests.HTTPMethod;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.flix29.besserTanken.Constants.API_KEY;
import static de.flix29.besserTanken.deserializer.CustomModelTypes.*;
import static de.flix29.besserTanken.kraftstoffbilliger.model.requests.Endpoints.*;
import static de.flix29.besserTanken.kraftstoffbilliger.model.requests.HTTPMethod.GET;
import static de.flix29.besserTanken.kraftstoffbilliger.model.requests.HTTPMethod.POST;

@Service
public class KraftstoffbilligerJob {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new CustomLocalDateTimeDeserializer())
            .registerTypeAdapter(FuelType.class, new CustomFuelTypeDeserializer())
            .registerTypeAdapter(FuelStation.class, new CustomFuelStationDeserializer())
            .registerTypeAdapter(PRICE_LIST_TYPE, new CustomPriceDeserializer())
            .registerTypeAdapter(OPENING_TIMES_LIST_TYPE, new CustomOpeningTimeDeserializer())
            .setPrettyPrinting()
            .create();

    private HttpResponse<String> sendHttpGETRequestWithResponse(Endpoints endpoint) throws IOException, InterruptedException {
        return sendHttpRequestWithResponse(endpoint, GET, null);
    }

    private HttpResponse<String> sendHttpPOSTRequestWithResponse(Endpoints endpoint, Map<String, String> parameter) throws IOException, InterruptedException {
        return sendHttpRequestWithResponse(endpoint, POST, parameter);
    }

    private HttpResponse<String> sendHttpRequestWithResponse(Endpoints endpoint, HTTPMethod httpMethod, Map<String, String> parameter) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint.getUrl()))
                .header("apikey", API_KEY);

        if (httpMethod == GET) {
            requestBuilder.GET();
        } else if(httpMethod == POST) {
            requestBuilder
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(createFormDataFromString(parameter)));
        }

        return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public List<FuelType> getFuelTypes() throws IOException, InterruptedException {
        var response = sendHttpGETRequestWithResponse(TYPES_ENDPOINT);
        var jsonArray = gson.fromJson(response.body(), JsonObject.class).get("types").getAsJsonArray();

        return gson.fromJson(jsonArray, FUEL_TYPE_LIST_TYPE);
    }

    public List<FuelStation> getFuelStations(@NotNull FuelType fuelType,
                                             @NotNull double lat,
                                             @NotNull double lon,
                                             String radius) throws IOException, InterruptedException {
        var formData = new HashMap<>(Map.of(
                "type", String.valueOf(fuelType.getId()),
                "lat", String.valueOf(lat),
                "lon", String.valueOf(lon)));

        if(radius != null) {
            formData.put("radius", radius);
        }

        var response = sendHttpPOSTRequestWithResponse(SEARCH_ENDPOINT, formData);
        var jsonArray = gson.fromJson(response.body(), JsonObject.class).get("results").getAsJsonArray();

        return gson.fromJson(jsonArray, FUEL_STATION_LIST_TYPE);
    }

    public List<FuelStation> getFuelStationRoute(@NotNull int lat,
                                                 @NotNull int lon,
                                                 @NotNull int lat2,
                                                 @NotNull int lon2,
                                                 @NotNull int type,
                                                 String map) throws IOException, InterruptedException {
        var formData = new HashMap<>(Map.of(
                "lat", String.valueOf(lat),
                "lon", String.valueOf(lon),
                "lat2", String.valueOf(lat2),
                "lon2", String.valueOf(lon2),
                "type", String.valueOf(type)));

        if(map != null) {
            formData.put("map", map);
        }

        var response = sendHttpPOSTRequestWithResponse(ROUTING_ENDPOINT, formData);
        JsonArray jsonArray = gson.fromJson(response.body(), JsonObject.class).get("results").getAsJsonArray();

        return gson.fromJson(jsonArray, FUEL_STATION_LIST_TYPE);
    }

    public FuelStationDetail getFuelStationDetails(@NotNull String id) throws IOException, InterruptedException {
        var formData = Map.of("id", id);

        var response = sendHttpPOSTRequestWithResponse(DETAILS_ENDPOINT, formData);
        var result = gson.fromJson(response.body(), JsonObject.class).get("result").getAsJsonArray().get(0);

        return gson.fromJson(result, FuelStationDetail.class);
    }

    private static String createFormDataFromString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        formData.forEach((key, value) -> {
            if (!formBodyBuilder.isEmpty()) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        });
        return formBodyBuilder.toString();
    }
}
