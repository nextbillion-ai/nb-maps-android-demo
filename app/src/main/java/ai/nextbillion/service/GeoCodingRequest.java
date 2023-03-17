package ai.nextbillion.service;

import android.location.Location;

import ai.nextbillion.geocoding.rest.GeoCodingService;
import ai.nextbillion.geocoding.rest.SearchResponse;
import ai.nextbillion.maps.Nextbillion;
import retrofit2.Callback;

/**
 * @author qiuyu
 * @Date 2022/7/1
 **/
public class GeoCodingRequest {

    public static void searchLocation(String query, Location location, Callback<SearchResponse> callback) {
        String at = String.format("%s,%s", location.getLatitude(), location.getLongitude());
        new GeoCodingService().getService().search(Nextbillion.getAccessKey(), query, at).enqueue(callback);
    }
}