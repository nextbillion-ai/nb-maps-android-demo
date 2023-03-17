package ai.nextbillion.nbaiapiclient;

import ai.nextbillion.api.models.directions.NBRoute;

public interface NBRouteLineCallback {
    void onRouteRendered(NBRoute route, NBRouteLine routeLine);

    void onError(Throwable throwable);
}
