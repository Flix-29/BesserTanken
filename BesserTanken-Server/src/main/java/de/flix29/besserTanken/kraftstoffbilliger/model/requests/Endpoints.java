package de.flix29.besserTanken.kraftstoffbilliger.model.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoints {

    BASIC_ENDPOINT("https://api.kraftstoffbilliger.de/v2", "basic"),
    SEARCH_ENDPOINT(BASIC_ENDPOINT.url + "/search", "search"),
    ROUTING_ENDPOINT(BASIC_ENDPOINT.url + "/routing", "routing"),
    DETAILS_ENDPOINT(BASIC_ENDPOINT.url + "/details", "details"),
    TYPES_ENDPOINT(BASIC_ENDPOINT.url + "/types", "types");

    private final String url;
    private final String endpoint;

}
