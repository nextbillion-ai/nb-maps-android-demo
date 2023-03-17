package ai.nextbillion.geocoding.rest;

import com.google.gson.GsonBuilder;

import ai.nextbillion.maps.Nextbillion;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeoCodingService {

    private boolean enableDebug;
    protected OkHttpClient okHttpClient;
    private okhttp3.Call.Factory callFactory;
    private GeoCodingAPI geocodingAPI;
    private Retrofit retrofit;

    public GeoCodingAPI getService() {
        if (geocodingAPI != null) {
            return geocodingAPI;
        }

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(Nextbillion.getBaseUri())
                .addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create()));

        if (getCallFactory() != null) {
            retrofitBuilder.callFactory(getCallFactory());
        } else {
            retrofitBuilder.client(getOkHttpClient());
        }

        retrofit = retrofitBuilder.build();
        geocodingAPI = retrofit.create(GeoCodingAPI.class);
        return geocodingAPI;
    }

    GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }


    public okhttp3.Call.Factory getCallFactory() {
        return callFactory;
    }

    public void setCallFactory(okhttp3.Call.Factory callFactory) {
        this.callFactory = callFactory;
    }


    protected synchronized OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            if (isEnableDebug()) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                httpClient.addInterceptor(logging);
                okHttpClient = httpClient.build();
            } else {
                okHttpClient = new OkHttpClient();
            }
        }
        return okHttpClient;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void enableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

}
