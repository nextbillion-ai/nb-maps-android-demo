package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FieldScore {
    /**
     * "country": 0,
     * "countryCode": 0,
     * "state": 0,
     * "stateCode": 0,
     * "county": 0,
     * "countyCode": 0,
     * "city": 0,
     * "district": 0,
     * "subdistrict": 0,
     * "streets": [
     * 0
     * ],
     * "block": 0,
     * "subblock": 0,
     * "houseNumber": 0,
     * "postalCode": 0,
     * "building": 0,
     * "unit": 0,
     * "placeName": 0,
     * "ontologyName": 0
     */

    @SerializedName("country")
    public double country;
    @SerializedName("countryCode")
    public double countryCode;
    @SerializedName("state")
    public double state;
    @SerializedName("county")
    public double county;
    @SerializedName("stateCode")
    public double stateCode;
    @SerializedName("countyCode")
    public double countyCode;
    @SerializedName("city")
    public double city;
    @SerializedName("district")
    public double district;
    @SerializedName("subdistrict")
    public double subdistrict;
    @SerializedName("block")
    public double block;
    @SerializedName("subblock")
    public double subblock;
    @SerializedName("houseNumber")
    public double houseNumber;
    @SerializedName("postalCode")
    public double postalCode;
    @SerializedName("building")
    public double building;
    @SerializedName("unit")
    public double unit;
    @SerializedName("placeName")
    public double placeName;
    @SerializedName("ontologyName")
    public double ontologyName;
    @SerializedName("streets")
    public List<Double> streets;
}
