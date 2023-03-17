package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class MapViewPort {
    @SerializedName("west")
    public double west;
    @SerializedName("south")
    public double south;
    @SerializedName("east")
    public double east;
    @SerializedName("north")
    public double north;
}
