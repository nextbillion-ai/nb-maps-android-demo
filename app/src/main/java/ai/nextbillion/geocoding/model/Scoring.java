package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class Scoring {
    @SerializedName("queryScore")
    public double queryScore;
    @SerializedName("fieldScore")
    public FieldScore fieldScore;
}
