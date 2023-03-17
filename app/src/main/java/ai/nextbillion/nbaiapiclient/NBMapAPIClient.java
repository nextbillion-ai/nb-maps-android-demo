package ai.nextbillion.nbaiapiclient;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import ai.nextbillion.api.NBMapAPI;
import ai.nextbillion.api.directions.NBDirections;
import ai.nextbillion.api.directions.NBDirectionsResponse;
import ai.nextbillion.api.distancematrix.NBDistanceMatrixResponse;
import ai.nextbillion.api.models.NBLocation;
import ai.nextbillion.api.models.directions.NBRoute;
import ai.nextbillion.api.snaptoroad.NBSnapToRoad;
import ai.nextbillion.api.snaptoroad.NBSnapToRoadResponse;
import ai.nextbillion.maps.Nextbillion;
import ai.nextbillion.maps.core.NextbillionMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NBMapAPIClient {

    private final NBMapAPI mapAPI;

    public NBMapAPIClient() {
        String key = Nextbillion.getAccessKey();
        String baseUrl = Nextbillion.getBaseUri();
        mapAPI = NBMapAPI.getInstance(key, baseUrl);
        mapAPI.setMode(NBDirectionsConstants.MODE_4W);
    }

    public NBMapAPIClient(String baseUrl) {
        String key = Nextbillion.getAccessKey();
        mapAPI = NBMapAPI.getInstance(key, baseUrl);
        mapAPI.setMode(NBDirectionsConstants.MODE_4W);
        mapAPI.setDebug(true);
    }

    public NBMapAPIClient setMode(String mode) {
        mapAPI.setMode(mode);
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Directions
    ///////////////////////////////////////////////////////////////////////////

    public Response<NBDirectionsResponse> getDirections(NBLocation origin, NBLocation dest) throws IOException {
        return mapAPI.getDirections(origin, dest);
    }

    public void enQueueGetDirections(NBLocation origin, NBLocation dest, Callback<NBDirectionsResponse> callback) throws IOException {
        mapAPI.enqueueGetDirections(origin, dest, callback);
    }

    public void showDirections(NBLocation origin, NBLocation dest, final String name, final NextbillionMap nextbillionMap, final Context context) {
        mapAPI.enqueueGetDirections(origin, dest, new Callback<NBDirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NBDirectionsResponse> call, @NonNull Response<NBDirectionsResponse> response) {
                NBDirectionsResponse directionsResponse = response.body();
                NBRouteLine routeLine = new NBRouteLine(nextbillionMap, context, name);
                if (directionsResponse != null && directionsResponse.routes() != null && !directionsResponse.routes().isEmpty()) {
                    routeLine.drawRoute(directionsResponse.routes().get(0));
                }
            }

            @Override
            public void onFailure(@NonNull Call<NBDirectionsResponse> call, @NonNull Throwable t) {

            }
        });
    }

    public static void showDirection(final Context context, final NextbillionMap nextbillionMap, final NBDirections directions, final NBRouteLineConfig routLineConfig, final NBRouteLineCallback callback) {
        directions.enqueueCall(new Callback<NBDirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NBDirectionsResponse> call, @NonNull Response<NBDirectionsResponse> response) {
                NBDirectionsResponse directionsResponse = response.body();
                NBRouteLine routeLine = new NBRouteLine(nextbillionMap, context, routLineConfig);
                if (directionsResponse != null && directionsResponse.routes() != null && !directionsResponse.routes().isEmpty()) {
                    NBRoute route = directionsResponse.routes().get(0);
                    routeLine.drawRoute(route);
                    if (callback != null) {
                        callback.onRouteRendered(route, routeLine);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("Failed to fetch directions"));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NBDirectionsResponse> call, @NonNull Throwable t) {
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }

    public void showDirections(NBLocation origin, NBLocation dest, final NBRouteLineConfig routLineConfig, final NextbillionMap nextbillionMap, final Context context, final NBRouteLineCallback callback) {
        mapAPI.enqueueGetDirections(origin, dest, new Callback<NBDirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NBDirectionsResponse> call, @NonNull Response<NBDirectionsResponse> response) {
                NBDirectionsResponse directionsResponse = response.body();
                NBRouteLine routeLine = new NBRouteLine(nextbillionMap, context, routLineConfig);
                if (directionsResponse != null && directionsResponse.routes() != null && !directionsResponse.routes().isEmpty()) {
                    NBRoute route = directionsResponse.routes().get(0);
                    routeLine.drawRoute(route);
                    if (callback != null) {
                        callback.onRouteRendered(route, routeLine);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("Failed to fetch directions"));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NBDirectionsResponse> call, @NonNull Throwable t) {
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //Distance Matrix
    ///////////////////////////////////////////////////////////////////////////

    public void enqueueGetDistanceMatrix(List<NBLocation> origins, List<NBLocation> destinations, Callback<NBDistanceMatrixResponse> callback) throws IOException {
        mapAPI.enqueueGetDistanceMatrix(origins, destinations, callback);
    }

    public Response<NBDistanceMatrixResponse> getDistanceMatrix(List<NBLocation> origins, List<NBLocation> destinations) throws IOException {
        return mapAPI.getDistanceMatrix(origins, destinations);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Matching
    ///////////////////////////////////////////////////////////////////////////

    public Response<NBSnapToRoadResponse> getSnapToRoad(List<NBLocation> path) throws IOException {
        return mapAPI.getSnapToRoad(path);
    }

    public void enqueueSnapToRoad(List<NBLocation> path, Callback<NBSnapToRoadResponse> callback) throws IOException {
        mapAPI.enqueueGetSnapToRoads(path, callback);
    }

    public void showMatchedRoute(List<NBLocation> path, final String name, final NextbillionMap map, final Context context) {
        mapAPI.enqueueGetSnapToRoads(path, new Callback<NBSnapToRoadResponse>() {
            @Override
            public void onResponse(@NonNull Call<NBSnapToRoadResponse> call, @NonNull Response<NBSnapToRoadResponse> response) {
                NBRouteLine routeLine = new NBRouteLine(map, context, name);
                if (response.body() != null) {
                    routeLine.drawMatchedRoute(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<NBSnapToRoadResponse> call, @NonNull Throwable t) {

            }
        });
    }


    public void showMatchedRoute(final Context context, final NextbillionMap map, List<NBLocation> path, final NBRouteLineConfig routLineConfig, final NBRouteLineCallback callback) {
        mapAPI.enqueueGetSnapToRoads(path, new Callback<NBSnapToRoadResponse>() {
            @Override
            public void onResponse(@NonNull Call<NBSnapToRoadResponse> call, @NonNull Response<NBSnapToRoadResponse> response) {
                NBRouteLine routeLine = new NBRouteLine(map, context, routLineConfig);
                if (response.body() != null) {
                    routeLine.drawMatchedRoute(response.body());
                    if (callback != null) {
                        callback.onRouteRendered(null, routeLine);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("Failed to fetch matching data"));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NBSnapToRoadResponse> call, @NonNull Throwable t) {
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }

    public static void showMatchedRoute(final Context context, final NextbillionMap map, NBSnapToRoad snapToRoad, final NBRouteLineConfig routLineConfig, final NBRouteLineCallback callback) {
        snapToRoad.enqueueCall(new Callback<NBSnapToRoadResponse>() {
            @Override
            public void onResponse(@NonNull Call<NBSnapToRoadResponse> call, @NonNull Response<NBSnapToRoadResponse> response) {
                NBRouteLine routeLine = new NBRouteLine(map, context, routLineConfig);
                if (response.body() != null) {
                    routeLine.drawMatchedRoute(response.body());
                    if (callback != null) {
                        callback.onRouteRendered(null, routeLine);
                    }
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("Failed to fetch matching data"));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NBSnapToRoadResponse> call, @NonNull Throwable t) {
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }
}
