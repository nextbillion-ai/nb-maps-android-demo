package ai.nextbillion.geocoding.rest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoCodingAPI {

    @GET("h/discover")
    Call<SearchResponse> search(
            @Query("key") String key,
            @Query("q") String query,
            @Query("at") String at
    );
}
