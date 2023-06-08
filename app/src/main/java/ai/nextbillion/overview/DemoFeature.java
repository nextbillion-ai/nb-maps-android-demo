package ai.nextbillion.overview;

import android.os.Parcel;
import android.os.Parcelable;

public class DemoFeature implements Parcelable {

    private final String name;
    private final String label;
    private final String description;
    private final String category;

    public DemoFeature(String name, String label, String description, String category) {
        this.name = name;
        this.label = label;
        this.description = description;
        this.category = category;
    }

    private DemoFeature(Parcel in) {
        name = in.readString();
        label = in.readString();
        description = in.readString();
        category = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        String[] split = name.split("\\.");
        return split[split.length - 1];
    }

    public String getLabel() {
        return label != null ? label : getSimpleName();
    }

    public String getDescription() {
        return description != null ? description : "-";
    }

    public String getCategory() {
        return category;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(label);
        out.writeString(description);
        out.writeString(category);
    }

    public static final Creator<DemoFeature> CREATOR
            = new Creator<DemoFeature>() {
        public DemoFeature createFromParcel(Parcel in) {
            return new DemoFeature(in);
        }

        public DemoFeature[] newArray(int size) {
            return new DemoFeature[size];
        }
    };
}
