package de.flix29.besserTanken.openDataSoft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.flix29.besserTanken.deserializer.CustomLocationDeserializer;
import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
public class OpenDataSoftRequests {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search?country=Germany&format=jsonv2&limit=1";
    private static final String PLZ_QUERY = "&postalcode=$plz$";
    private static final String PLZ_NAME_QUERY = "&city=$city$";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(SimpleLocation.class, new CustomLocationDeserializer())
            .setPrettyPrinting()
            .create();

    public SimpleLocation getCoordsFromPlz(int plz) {
        return getCoordsFromPlzAndPlzName(plz, null);
    }

    public SimpleLocation getCoordsFromPlzName(String city) {
        return getCoordsFromPlzAndPlzName(0, city);
    }

    public SimpleLocation getCoordsFromPlzAndPlzName(int plz, String city) {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(plz, city)));

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        SimpleLocation result;
        try {
            result = gson.fromJson(response.body(), SimpleLocation.class);
            log.info("Found results for plz: {} and city: {}", plz, city);
            return result;
        } catch (Exception e) {
            log.error("Error while parsing response: {}", response.body(), e);
            return null;
        }
    }

    private String buildUrl(int plz, String city) {
        StringBuilder queryString = new StringBuilder(BASE_URL);
        if (plz != 0) {
            queryString.append(PLZ_QUERY.replace("$plz$", String.valueOf(plz)));
        }

        if (city != null) {
            queryString.append(PLZ_NAME_QUERY.replace("$city$", city));
        }

        if (plz == 0 && city == null) {
            throw new IllegalArgumentException("Either plz or city must be set");
        }

        return queryString.toString();
    }
}
