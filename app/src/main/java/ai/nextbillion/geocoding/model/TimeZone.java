package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class TimeZone {
    @SerializedName("utcOffset")
    public String utcOffset;
    @SerializedName("name")
    public String name;
}
