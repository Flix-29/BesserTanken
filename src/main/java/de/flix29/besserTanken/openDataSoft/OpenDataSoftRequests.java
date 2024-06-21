package de.flix29.besserTanken.openDataSoft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.flix29.besserTanken.deserializer.CustomLocationDeserializer;
import de.flix29.besserTanken.model.openDataSoft.Location;
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

import static de.flix29.besserTanken.deserializer.CustomModelTypes.LOCATION_TYPE;

@Service
public class OpenDataSoftRequests {

    private final String BASE_URL = "https://public.opendatasoft.com/api/explore/v2.1/catalog/datasets/georef-germany-postleitzahl/records?";
    private final String BASE_QUERY = "select=name,plz_name,geo_point_2d&offset=$offset$";
    private final String PLZ_QUERY = "&where=name=%22$plz$%22";
    private final String PLZ_NAME_QUERY = "&where=plz_name=%22$plz_name$%22";

    private final Logger LOGGER = LoggerFactory.getLogger(OpenDataSoftRequests.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Location.class, new CustomLocationDeserializer())
            .setPrettyPrinting()
            .create();

    public List<Location> getCoordsFromPlz(int plz) {
        return getCoordsFromPlzAndPlzName(plz, null, 0);
    }

    public List<Location> getCoordsFromPlzName(String plz_name) {
        return getCoordsFromPlzAndPlzName(0, plz_name, 0);
    }

    public List<Location> getCoordsFromPlzAndPlzName(int plz, String plz_name, int offset) {
        var url = buildUrl(plz, plz_name, offset);
        var requestBuilder = HttpRequest.newBuilder().uri(URI.create(BASE_URL + url));

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        int count;
        List<Location> result;
        try {
            var jsonObject = gson.fromJson(response.body(), JsonObject.class);
            count = jsonObject.get("total_count").getAsInt();
            var jsonArray = jsonObject.get("results").getAsJsonArray();

            result = gson.fromJson(jsonArray, LOCATION_TYPE);
        } catch (Exception e) {
            LOGGER.error("Error while parsing response: {}", response.body(), e);
            return Collections.emptyList();
        }

        if (count > 100) {
            result.addAll(getCoordsFromPlzAndPlzName(plz, plz_name, ++offset));
        }

        LOGGER.info("Found {} results for plz: {} and plz_name: {}", result.size(), plz, plz_name);
        return result;
    }

    private String buildUrl(int plz, String plz_name, int offset) {
        StringBuilder queryString = new StringBuilder(BASE_QUERY.replace("$offset$", String.valueOf(offset)));

        if (plz != 0) {
            queryString.append(PLZ_QUERY.replace("$plz$", String.valueOf(plz)));
        }

        if (plz_name != null) {
            queryString.append(PLZ_NAME_QUERY.replace("$plz_name$", plz_name));
        }

        if (plz == 0 && plz_name == null) {
            throw new IllegalArgumentException("Either plz or plz_name must be set");
        }

        return queryString.toString();
    }

}
