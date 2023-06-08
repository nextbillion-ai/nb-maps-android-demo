package ai.nextbillion;

public class StyleConstants {

    /**
     * Nbmap Streets: A complete basemap, perfect for incorporating your own data. Using this
     * constant means your map style will always use the latest version and may change as we
     * improve the style.
     */
    public static final String NBMAP_STREETS = "https://api.nextbillion.io/maps/streets/style.json";

    /**
     * Outdoors: A general-purpose style tailored to outdoor activities. Using this constant means
     * your map style will always use the latest version and may change as we improve the style.
     */
    public static final String OUTDOORS = "https://api.nextbillion.io/maps/streets/style.json";

    /**
     * Light: Subtle light backdrop for data visualizations. Using this constant means your map
     * style will always use the latest version and may change as we improve the style.
     * TODO: the value should be replaced with a multi-type source
     */
    public static final String LIGHT = "https://api.nextbillion.io/maps/streets/style.json";

    /**
     * Dark: Subtle dark backdrop for data visualizations. Using this constant means your map style
     * will always use the latest version and may change as we improve the style.
     * TODO: the value should be replaced with a multi-type source
     */
    public static final String DARK = "https://api.nextbillion.io/maps/dark/style.json";

    /**
     * Satellite: A beautiful global satellite and aerial imagery layer. Using this constant means
     * your map style will always use the latest version and may change as we improve the style.
     */
    public static final String SATELLITE = "https://api.nextbillion.io/tt/style/1/style/20.3.2-3?map=hybrid_night";

    /**
     * Satellite Streets: Global satellite and aerial imagery with unobtrusive labels. Using this
     * constant means your map style will always use the latest version and may change as we
     * improve the style.
     */
    public static final String SATELLITE_STREETS = "https://api.nextbillion.io/tt/style/1/style/20.3.2-3?map=hybrid_night";

    // TODO: should be replaced with a multi-type source
    public static final String TRAFFIC_DAY = "https://api.nextbillion.io/tt/style/1/style/20.3.2-3?map=basic_main&traffic_flow=flow_relative0";

    public static final String TRAFFIC_NIGHT = "https://api.nextbillion.io/tt/style/1/style/20.3.2-3?map=basic_night&traffic_flow=flow_relative0-dark";
}
