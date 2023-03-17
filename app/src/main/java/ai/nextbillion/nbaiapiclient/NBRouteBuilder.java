package ai.nextbillion.nbaiapiclient;


import static ai.nextbillion.maps.style.expressions.Expression.exponential;
import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.expressions.Expression.interpolate;
import static ai.nextbillion.maps.style.expressions.Expression.literal;
import static ai.nextbillion.maps.style.expressions.Expression.match;
import static ai.nextbillion.maps.style.expressions.Expression.stop;
import static ai.nextbillion.maps.style.expressions.Expression.zoom;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconAllowOverlap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconIgnorePlacement;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconPitchAlignment;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconSize;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineCap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineJoin;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineWidth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;


import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.R;
import ai.nextbillion.api.models.NBLocation;
import ai.nextbillion.api.models.directions.NBRoute;
import ai.nextbillion.api.models.directions.NBRouteLeg;
import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.FeatureCollection;
import ai.nextbillion.kits.geojson.LineString;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.style.expressions.Expression;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.LineLayer;
import ai.nextbillion.maps.style.layers.Property;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.style.sources.GeoJsonSource;

public class NBRouteBuilder {
    private Context context;
    private GeoJsonSource routeSource;
    private GeoJsonSource wayPointsSource;
    private Layer routeLayer;
    private Layer wayPointsLayer;
    private Style mStyle;
    private NextbillionMap mMap;
    private String mSourceID;
    private String mLayerID;
    private float mLineWidth = 3.0f;
    private String mLineColor = "#f54242";
    private String mLineCap = Property.LINE_CAP_ROUND;
    private String mLineJoin = Property.LINE_JOIN_ROUND;

    public NBRouteBuilder(@NonNull Context context, @NonNull String sourceId, @NonNull String layerId) {
        this.context = context;
        mSourceID = sourceId;
        mLayerID = layerId;
    }

    static final String ORIGIN_MARKER_NAME = "originMarker";
    static final String DESTINATION_MARKER_NAME = "destinationMarker";

    public void init(NextbillionMap map) {
        mMap = map;
        mStyle = map.getStyle();
        GeoJsonOptions routeLineGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
        GeoJsonOptions wayPointGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
        routeSource = new GeoJsonSource(mSourceID, routeLineGeoJsonOptions);
        wayPointsSource = new GeoJsonSource("wp_" + mSourceID, wayPointGeoJsonOptions);
        mStyle.addSource(routeSource);
        mStyle.addSource(wayPointsSource);

        routeLayer = new LineLayer(mLayerID, mSourceID)
                .withProperties(
                        lineWidth(mLineWidth),
                        lineColor(mLineColor),
                        lineCap(mLineCap),
                        lineJoin(mLineJoin)
                );
        mStyle.addLayer(routeLayer);
        int originWaypointIcon = R.drawable.blue_marker;
        int destinationWaypointIcon = R.drawable.red_marker;
        Bitmap bitmap = getBitmapFromDrawable(AppCompatResources.getDrawable(context, originWaypointIcon));
        mStyle.addImage(ORIGIN_MARKER_NAME, bitmap);
        bitmap = getBitmapFromDrawable(AppCompatResources.getDrawable(context, destinationWaypointIcon));
        mStyle.addImage(DESTINATION_MARKER_NAME, bitmap);

        wayPointsLayer = new SymbolLayer("wp_" + mLayerID, "wp_" + mSourceID).withProperties(
                iconImage(
                        match(
                                Expression.toString(get(WAYPOINT_PROPERTY_KEY)), literal(ORIGIN_MARKER_NAME),
                                stop(WAYPOINT_ORIGIN_VALUE, literal(ORIGIN_MARKER_NAME)),
                                stop(WAYPOINT_DESTINATION_VALUE, literal(DESTINATION_MARKER_NAME))
                        )),
                iconSize(
                        interpolate(
                                exponential(1.5f), zoom(),
                                stop(0f, 0.6f),
                                stop(10f, 0.8f),
                                stop(12f, 1.3f),
                                stop(22f, 2.8f)
                        )
                ),
                iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        mStyle.addLayer(wayPointsLayer);
    }

    public void drawRoute(NBRoute route) {
        drawRoute(generateFeatureCollection(route));
        drawWayPoints(generateWayPointFeatureCollection(route));
        animateCamera(mMap, route);
    }

    public void setLineWidth(float mLineWidth) {
        this.mLineWidth = mLineWidth;
        if (routeLayer != null) {
            routeLayer.setProperties(lineWidth(mLineWidth));
        }
    }

    public void setLineColor(String mLineColor) {
        this.mLineColor = mLineColor;
        if (routeLayer != null) {
            routeLayer.setProperties(lineColor(mLineColor));
        }
    }

    public void setLineCap(String mLineCap) {
        this.mLineCap = mLineCap;
        if (routeLayer != null) {
            routeLayer.setProperties(lineCap(mLineCap));
        }
    }

    public void setLineJoin(String mLineJoin) {
        this.mLineJoin = mLineJoin;
        if (routeLayer != null) {
            routeLayer.setProperties(lineJoin(mLineJoin));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void drawRoute(FeatureCollection featureCollection) {
        routeSource.setGeoJson(featureCollection);
    }

    private void drawWayPoints(FeatureCollection featureCollection) {
        wayPointsSource.setGeoJson(featureCollection);
    }

    private FeatureCollection generateFeatureCollection(NBRoute route) {
        final List<Feature> features = new ArrayList<>();
        LineString routeGeometry = LineString.fromPolyline(route.geometry(), 5);
        Feature routeFeature = Feature.fromGeometry(routeGeometry);
        features.add(routeFeature);
        return FeatureCollection.fromFeatures(features);
    }

    private NBLocation calcMidLocation(NBLocation origin, NBLocation dest) {
        double midLat = (origin.latitude + dest.latitude) / 2;
        double midLng = (origin.longitude + dest.longitude) / 2;
        return new NBLocation(midLat, midLng);
    }

    public void animateCamera(NextbillionMap map, NBRoute route) {
        moveCamera(map, calcMidLocation(route.startLocation(), route.endLocation()));
    }

    private void moveCamera(NextbillionMap map, NBLocation location) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(location.latitude, location.longitude))
                .zoom(10.5)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 600);
    }

    private FeatureCollection generateWayPointFeatureCollection(NBRoute route) {
        final List<Feature> wayPointFeatures = new ArrayList<>();
        for (NBRouteLeg leg : route.legs()) {
            wayPointFeatures.add(buildWayPointFeatureFromLeg(leg, 0));
            wayPointFeatures.add(buildWayPointFeatureFromLeg(leg, leg.steps().size() - 1));
        }
        return FeatureCollection.fromFeatures(wayPointFeatures);
    }

    static final String WAYPOINT_PROPERTY_KEY = "wayPoint";
    static final String WAYPOINT_ORIGIN_VALUE = "origin";
    static final String WAYPOINT_DESTINATION_VALUE = "destination";

    private Feature buildWayPointFeatureFromLeg(NBRouteLeg leg, int index) {
        Feature feature = Feature.fromGeometry(Point.fromLngLat(
                leg.steps().get(index).startLocation().longitude,
                leg.steps().get(index).startLocation().latitude
        ));
        feature.addStringProperty(WAYPOINT_PROPERTY_KEY, index == 0 ? WAYPOINT_ORIGIN_VALUE : WAYPOINT_DESTINATION_VALUE);
        return feature;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public void remove() {
        mStyle.removeLayer(routeLayer);
        mStyle.removeLayer(wayPointsLayer);
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }
}
