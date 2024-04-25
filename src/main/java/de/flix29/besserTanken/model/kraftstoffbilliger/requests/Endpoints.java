package de.flix29.besserTanken.model.kraftstoffbilliger.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoints {

    BASIC_ENDPOINT("https://api.kraftstoffbilliger.de/v2", "basic"),
    SEARCH_ENDPOINT(BASIC_ENDPOINT.getUrl() + "/search", "search"),
    ROUTING_ENDPOINT(BASIC_ENDPOINT.getUrl() + "/routing", "routing"),
    DETAILS_ENDPOINT(BASIC_ENDPOINT.getUrl() + "/details", "details"),
    TYPES_ENDPOINT(BASIC_ENDPOINT.getUrl() + "/types", "types");

    private final String url;
    private final String endpoint;

}
