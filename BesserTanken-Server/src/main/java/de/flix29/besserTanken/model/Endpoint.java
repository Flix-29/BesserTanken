package de.flix29.besserTanken.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {

    BASIC_ENDPOINT("https://api.kraftstoffbilliger.de/v2", "basic"),
    SEARCH_ENDPOINT("https://api.kraftstoffbilliger.de/v2/search", "search"),
    ROUTING_ENDPOINT("https://api.kraftstoffbilliger.de/v2/routing", "routing"),
    DETAILS_ENDPOINT("https://api.kraftstoffbilliger.de/v2/details", "details"),
    TYPES_ENDPOINT("https://api.kraftstoffbilliger.de/v2/types", "types");

    private final String url;
    private final String endpoint;

}
