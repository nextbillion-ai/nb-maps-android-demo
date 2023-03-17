package ai.nextbillion.geocoding.rest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ai.nextbillion.geocoding.model.SearchItem;


public class SearchResponse {
    @SerializedName("items")
    public List<SearchItem> items;

    @Override
    public String toString() {
        return "SearchResponse{" +
                "items=" + items +
                '}';
    }
}
