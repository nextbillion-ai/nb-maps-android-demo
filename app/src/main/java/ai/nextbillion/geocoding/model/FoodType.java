package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class FoodType {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("primary")
    public boolean primary;
}
