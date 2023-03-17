package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class Position {

    @SerializedName("lat")
    public double lat;
    @SerializedName("lng")
    public double lng;

    @Override
    public String toString() {
        return "Position{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}

