package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

public class Address {
    /**
     * "label": "string",
     * "countryCode": "string",
     * "countryName": "string",
     * "stateCode": "string",
     * "state": "string",
     * "countyCode": "string",
     * "county": "string",
     * "city": "string",
     * "district": "string",
     * "subdistrict": "string",
     * "street": "string",
     * "block": "string",
     * "subblock": "string",
     * "postalCode": "string",
     * "houseNumber": "string",
     * "building": "string"
     */

    @SerializedName("label")
    public String label;
    @SerializedName("countryCode")
    public String countryCode;
    @SerializedName("countryName")
    public String countryName;
    @SerializedName("stateCode")
    public String stateCode;
    @SerializedName("state")
    public String state;
    @SerializedName("countyCode")
    public String countyCode;
    @SerializedName("county")
    public String county;
    @SerializedName("city")
    public String city;
    @SerializedName("district")
    public String district;
    @SerializedName("subdistrict")
    public String subdistrict;
    @SerializedName("street")
    public String street;
    @SerializedName("block")
    public String block;
    @SerializedName("subblock")
    public String subblock;
    @SerializedName("postalCode")
    public String postalCode;
    @SerializedName("houseNumber")
    public String houseNumber;
    @SerializedName("building")
    public String building;

    public String getSubLabel(){
        String ignoredCode = ", " + this.stateCode + " " + this.postalCode + ", " + this.countryName;
        return this.label != null ? this.label.replace(ignoredCode,"") : "";
    }

    @Override
    public String toString() {
        return "Address{" +
                "label='" + label + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", state='" + state + '\'' +
                ", countyCode='" + countyCode + '\'' +
                ", county='" + county + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", subdistrict='" + subdistrict + '\'' +
                ", street='" + street + '\'' +
                ", block='" + block + '\'' +
                ", subblock='" + subblock + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", houseNumber='" + houseNumber + '\'' +
                ", building='" + building + '\'' +
                '}';
    }
}
