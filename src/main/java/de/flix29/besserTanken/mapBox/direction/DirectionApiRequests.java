package de.flix29.besserTanken.mapBox.direction;

import de.flix29.besserTanken.model.mapBox.Route;
import de.flix29.besserTanken.model.openDataSoft.SimpleLocation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DirectionApiRequests {

    private final DirectionApiJob directionApiJob;

    public DirectionApiRequests(DirectionApiJob directionApiJob) {
        this.directionApiJob = directionApiJob;
    }

    public <T extends SimpleLocation> double getRealDistance(T start, T end) {
        List<Route> routes;
        try {
            routes = directionApiJob.getRoutes(start, end);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return routes.stream()
                .mapToDouble(route -> route.getDistance() / 1000.0)
                .min()
                .orElseThrow();
    }

}
