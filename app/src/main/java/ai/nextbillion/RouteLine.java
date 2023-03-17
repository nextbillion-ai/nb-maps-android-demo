package ai.nextbillion;

import java.util.List;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.LineString;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.LineLayer;
import ai.nextbillion.maps.style.layers.Property;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.style.sources.GeoJsonSource;

import static ai.nextbillion.maps.style.layers.PropertyFactory.lineCap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineJoin;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineWidth;

public class RouteLine {

    private String routeLineId;

    private String routeLineSourceId;
    private String routeLineLayerId;

    private GeoJsonSource routeSource;
    private Layer routeLayer;


    private float lineWidth = 5.0f;
    private String lineColor = "#6200EE";
    private String lineCap = Property.LINE_CAP_ROUND;
    private String lineJoin = Property.LINE_JOIN_ROUND;

    public RouteLine(Style style, String routeLineId) {
        this.routeLineId = routeLineId;
        routeLineSourceId = routeLineId + "_source";
        routeLineLayerId = routeLineId + "_layer";
        init(style);
    }

    private void init(Style style) {
        style.removeLayer(routeLineLayerId);
        style.removeSource(routeLineSourceId);
        GeoJsonOptions routeLineGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
        routeSource = new GeoJsonSource(routeLineSourceId, routeLineGeoJsonOptions);
        routeLayer = new LineLayer(routeLineLayerId, routeLineSourceId).withProperties(
                lineWidth(lineWidth),
                lineColor(lineColor),
                lineCap(lineCap),
                lineJoin(lineJoin)
        );
        style.addSource(routeSource);
        style.addLayer(routeLayer);
    }

    public void drawRouteLine(String geometry) {
        routeSource.setGeoJson(generateRouteLineFeature(geometry, 5));
    }

    private Feature generateRouteLineFeature(String geometry, int precision) {
        LineString routeGeometry = LineString.fromPolyline(geometry, precision);
        return Feature.fromGeometry(routeGeometry);
    }

    public void updateRoute(List<Point> newRoute) {
        LineString lineString = LineString.fromLngLats(newRoute);
        Feature routeFeature = Feature.fromGeometry(lineString);
        this.routeSource.setGeoJson(routeFeature);
    }
}
