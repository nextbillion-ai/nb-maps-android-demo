package ai.nextbillion.geocoding.model;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class LocationSuggestion implements SearchSuggestion, Comparable<LocationSuggestion> {

    public String title;
    public double lat;
    public double lng;
    public double score;
    public String subTitle;

    LocationSuggestion(Parcel in) {
        title = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        score = in.readDouble();
        subTitle = in.readString();
    }

    public LocationSuggestion(SearchItem item) {
        title = item.title;
        if (item.position != null) {
            lat = item.position.lat;
            lng = item.position.lng;
        }
        if (item.scoring != null) {
            score = item.scoring.queryScore;
        }
        if (item.address != null) {
            subTitle = item.address.getSubLabel();
        }
    }


    private static final Creator<LocationSuggestion> CREATOR = new Creator<LocationSuggestion>() {
        @Override
        public LocationSuggestion createFromParcel(Parcel in) {
            return new LocationSuggestion(in);
        }

        @Override
        public LocationSuggestion[] newArray(int size) {
            return new LocationSuggestion[size];
        }
    };


    @Override
    public String getBody() {
        return subTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeDouble(score);
        dest.writeString(subTitle);
    }

    @Override
    public int compareTo(LocationSuggestion o) {
        if (o == null) {
            return 1;
        }
        return Double.compare(this.score, o.score);
    }
}
