package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class StreetInfo {
    /**
     * "baseName": "string",
     * "streetType": "string",
     * "streetTypePrecedes": true,
     * "streetTypeAttached": true,
     * "prefix": "string",
     * "suffix": "string",
     * "direction": "string",
     * "language": "string"
     */

    @SerializedName("baseName")
    public String baseName;
    @SerializedName("streetType")
    public String streetType;
    @SerializedName("streetTypePrecedes")
    public boolean streetTypePrecedes;
    @SerializedName("streetTypeAttached")
    public boolean streetTypeAttached;


    @SerializedName("prefix")
    public String prefix;
    @SerializedName("suffix")
    public boolean suffix;

    @SerializedName("direction")
    public String direction;
    @SerializedName("language")
    public String language;
}
