package ai.nextbillion.geocoding.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchItem {
    @SerializedName("title")
    public String title;
    @SerializedName("id")
    public String id;
    @SerializedName("politicalView")
    public String politicalView;
    @SerializedName("resultType")
    public String resultType;
    @SerializedName("houseNumberType")
    public String houseNumberType;
    @SerializedName("addressBlockType")
    public String addressBlockType;
    @SerializedName("localityType")
    public String localityType;

    @SerializedName("administrativeArea")
    public String administrativeArea;
    @SerializedName("administrativeAreaType")
    public String administrativeAreaType;

    @SerializedName("houseNumberFallback")
    public boolean houseNumberFallback;

    @SerializedName("distance")
    public Double distance;


    @SerializedName("address")
    public Address address;
    @SerializedName("position")
    public Position position;
    @SerializedName("access")
    public List<Position> access;
    @SerializedName("mapView")
    public MapViewPort mapView;
    @SerializedName("foodTypes")
    public List<FoodType> foodTypes;
    @SerializedName("categories")
    public List<Category> categories;

    @SerializedName("timeZone")
    public TimeZone timeZone;
    @SerializedName("scoring")
    public Scoring scoring;
    @SerializedName("parsing")
    public Parsing parsing;
    @SerializedName("streetInfo")
    public StreetInfo streetInfo;
    @SerializedName("countryInfo")
    public CountryInfo countryInfo;

    @Override
    public String toString() {
        return "SearchItem{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", politicalView='" + politicalView + '\'' +
                ", resultType='" + resultType + '\'' +
                ", houseNumberType='" + houseNumberType + '\'' +
                ", addressBlockType='" + addressBlockType + '\'' +
                ", localityType='" + localityType + '\'' +
                ", administrativeArea='" + administrativeArea + '\'' +
                ", administrativeAreaType='" + administrativeAreaType + '\'' +
                ", houseNumberFallback=" + houseNumberFallback +
                ", distance=" + distance +
                ", address=" + address +
                ", position=" + position +
                ", access=" + access +
                ", mapView=" + mapView +
                ", foodTypes=" + foodTypes +
                ", categories=" + categories +
                ", timeZone=" + timeZone +
                ", scoring=" + scoring +
                ", parsing=" + parsing +
                ", streetInfo=" + streetInfo +
                ", countryInfo=" + countryInfo +
                '}';
    }
}
