package ai.nextbillion.nbaiapiclient;


import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineCap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineJoin;
import static ai.nextbillion.maps.style.layers.PropertyFactory.lineWidth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;


import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.R;
import ai.nextbillion.api.models.NBLocation;
import ai.nextbillion.api.models.NBSnappedPoint;
import ai.nextbillion.api.models.directions.NBRoute;
import ai.nextbillion.api.snaptoroad.NBSnapToRoadResponse;
import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.FeatureCollection;
import ai.nextbillion.kits.geojson.LineString;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.kits.turf.TurfMeasurement;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.LineLayer;
import ai.nextbillion.maps.style.layers.Property;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.style.sources.GeoJsonSource;

public class NBRouteLine {

    private GeoJsonSource routeSource;
    private GeoJsonSource originSource;
    private GeoJsonSource destinationSource;
    private NextbillionMap mMap;
    private Layer routeLayer;
    private Layer originLayer;
    private Layer destinationLayer;
    private String routeLayerId, originLayerId, destinationLayerId, routeSourceId, originSourceId, destinationSourceId, originIconId, destIconId;

    private float mLineWidth = 3.0f;
    private String mLineColor = "#f54242";
    private String mLineCap = Property.LINE_CAP_ROUND;
    private String mLineJoin = Property.LINE_JOIN_ROUND;

    @DrawableRes
    private int originalIcon = R.drawable.blue_marker;

    @DrawableRes
    private int destinationIcon = R.drawable.red_marker;

    public NBRouteLine(@NonNull NextbillionMap map, @NonNull Context context, @NonNull String name) {
        initIds(map, name);
        init(map, context);
    }

    public NBRouteLine(@NonNull NextbillionMap map, @NonNull Context context, @NonNull NBRouteLineConfig config) {
        initIds(map, config.getRouteName());
        mLineCap = config.getLineCap();
        mLineWidth = config.getLineWidth();
        mLineColor = config.getLineColor();
        mLineJoin = config.getLineJoin();
        originalIcon = config.getOriginalIcon();
        destinationIcon = config.getDestinationIcon();
        init(map, context);
    }

    private void initIds(NextbillionMap map, String name) {
        mMap = map;
        routeLayerId = name + "_route_layer_id";
        routeSourceId = name + "_route_source_id";
        originLayerId = name + "_origin_layer_id";
        originSourceId = name + "_origin_source_id";
        destinationLayerId = name + "_destination_layer_id";
        destinationSourceId = name + "_destination_source_id";
        originIconId = name + "_origin_icon_id";
        destIconId = name + "_dest_icon_id";
    }

    public void init(NextbillionMap map, Context context) {
        Style style = map.getStyle();
        if (style == null) {
            return;
        }
        updateIcons(context, map);
        GeoJsonOptions routeLineGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
        GeoJsonOptions originGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);
        GeoJsonOptions destinationGeoJsonOptions = new GeoJsonOptions().withMaxZoom(16);

        routeSource = new GeoJsonSource(routeSourceId, routeLineGeoJsonOptions);
        originSource = new GeoJsonSource(originSourceId, originGeoJsonOptions);
        destinationSource = new GeoJsonSource(destinationSourceId, destinationGeoJsonOptions);
        routeLayer = new LineLayer(routeLayerId, routeSourceId).withProperties(
                lineWidth(mLineWidth),
                lineColor(mLineColor),
                lineCap(mLineCap),
                lineJoin(mLineJoin)
        );

        originLayer = new SymbolLayer(originLayerId, originSourceId).withProperties(
                iconImage(originIconId)
        );
        destinationLayer = new SymbolLayer(destinationLayerId, destinationSourceId).withProperties(
                iconImage(destIconId)
        );

        style.removeLayer(routeLayerId);
        style.removeLayer(originLayerId);
        style.removeLayer(destinationLayerId);
        style.removeSource(routeSourceId);
        style.removeSource(originSourceId);
        style.removeSource(destinationSourceId);

        style.addSource(routeSource);
        style.addSource(originSource);
        style.addSource(destinationSource);
        style.addLayer(routeLayer);
        style.addLayer(originLayer);
        style.addLayer(destinationLayer);

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public void updateIcons(Context context, NextbillionMap map) {
        Style style = map.getStyle();
        if (style == null) {
            return;
        }

        style.removeImage(originIconId);
        style.removeImage(destIconId);

        Bitmap bitmap = getBitmapFromDrawable(AppCompatResources.getDrawable(context, originalIcon));
        style.addImage(originIconId, bitmap);
        bitmap = getBitmapFromDrawable(AppCompatResources.getDrawable(context, destinationIcon));
        style.addImage(destIconId, bitmap);
    }

    public void setLineWidth(float lineWidth) {
        this.mLineWidth = lineWidth;
        routeLayer.setProperties(
                lineWidth(mLineWidth),
                lineColor(mLineColor),
                lineCap(mLineCap),
                lineJoin(mLineJoin));
    }

    public void setLineColor(String lineColor) {
        this.mLineColor = lineColor;
        routeLayer.setProperties(
                lineWidth(mLineWidth),
                lineColor(mLineColor),
                lineCap(mLineCap),
                lineJoin(mLineJoin));
    }


    public void setLineCap(String mLineCap) {
        this.mLineCap = mLineCap;

        routeLayer.setProperties(
                lineWidth(mLineWidth),
                lineColor(mLineColor),
                lineCap(mLineCap),
                lineJoin(mLineJoin));
    }

    public void setLineJoin(String lineJoin) {
        this.mLineJoin = lineJoin;
        routeLayer.setProperties(
                lineWidth(mLineWidth),
                lineColor(mLineColor),
                lineCap(mLineCap),
                lineJoin(mLineJoin));
    }

    public void setOriginIcon(Context context, NextbillionMap nbmapMap, @DrawableRes int resId) {
        originalIcon = resId;
        Style style = nbmapMap.getStyle();
        if (style == null) {
            return;
        }
        style.removeImage(originIconId);

        Bitmap bitmap = getBitmapFromDrawable(AppCompatResources.getDrawable(context, originalIcon));
        style.addImage(originIconId, bitmap);
    }

    public void setDestinationIcon(Context context, NextbillionMap nbmapMap, @DrawableRes int resId) {
        destinationIcon = resId;
        Style style = nbmapMap.getStyle();
        if (style == null) {
            return;
        }
        style.removeImage(destIconId);
        Bitmap bitmap = getBitmapFromDrawable(AppCompatResources.getDrawable(context, destinationIcon));
        style.addImage(destIconId, bitmap);
    }

    public void remove() {
        mMap.getStyle().removeLayer(routeLayerId);
        mMap.getStyle().removeLayer(originLayerId);
        mMap.getStyle().removeLayer(destinationLayerId);

        mMap.getStyle().removeSource(routeSourceId);
        mMap.getStyle().removeSource(originSourceId);
        mMap.getStyle().removeSource(destinationSourceId);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Render
    ///////////////////////////////////////////////////////////////////////////

    public void drawRoute(NBRoute route) {
        String geometry = route.geometry();
        drawRoute(geometry, route.legs().get(0).startLocation(), route.legs().get(0).endLocation());
        moveCamera(mMap, route.startLocation(), route.endLocation());
    }


    public void drawMatchedRoute(NBSnapToRoadResponse snapToRoadResponse) {
        if (snapToRoadResponse != null && "Ok".equals(snapToRoadResponse.status()) && snapToRoadResponse.snappedPoints() != null && snapToRoadResponse.geometry() != null) {
            List<String> geometries = snapToRoadResponse.geometry();
            List<NBLocation> wayPoints = new ArrayList<>();
            NBLocation origin = snapToRoadResponse.snappedPoints().get(0).location;
            NBLocation destination = snapToRoadResponse.snappedPoints().get(snapToRoadResponse.snappedPoints().size() - 1).location;

            for (NBSnappedPoint point : snapToRoadResponse.snappedPoints()) {
                wayPoints.add(point.location);
            }

            drawRoute(geometries, wayPoints);
            if (snapToRoadResponse.snappedPoints().size() > 0) {
                moveCamera(mMap, origin, destination);
            }
        }
    }

    public void drawRoute(String geometry, NBLocation origin, NBLocation destination) {
        FeatureCollection routeFeatureCollection = generateFeatureFromGeometry(geometry);
        routeSource.setGeoJson(routeFeatureCollection);
        originSource.setGeoJson(generateFeatureCollectionFromLocation(origin));
        destinationSource.setGeoJson(generateFeatureCollectionFromLocation(destination));
    }

    public void updateRoute(List<Point> newRoute) {
        LineString lineString = LineString.fromLngLats(newRoute);
        Feature routeFeature = Feature.fromGeometry(lineString);
        routeSource.setGeoJson(routeFeature);
    }

    public void drawRoute(List<String> geometries, List<NBLocation> wayPoints) {
        List<Feature> features = new ArrayList<>();
        if (geometries != null) {
            for (String geometry : geometries) {
                FeatureCollection routeFeatureCollection = generateFeatureFromGeometry(geometry);
                features.addAll(routeFeatureCollection.features());
            }
        }
        FeatureCollection routeFeatureCollection = FeatureCollection.fromFeatures(features);
        routeSource.setGeoJson(routeFeatureCollection);
        if (wayPoints != null) {
            FeatureCollection wayPointsFeatureCollection = generateFeatureCollectionFromPoints(wayPoints);
            originSource.setGeoJson(wayPointsFeatureCollection);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Camera
    ///////////////////////////////////////////////////////////////////////////

    private NBLocation calcMidLocation(NBLocation origin, NBLocation dest) {
        double midLat = (origin.latitude + dest.latitude) / 2;
        double midLng = (origin.longitude + dest.longitude) / 2;
        return new NBLocation(midLat, midLng);
    }

    private double calcZoomLevel(NBLocation origin, NBLocation dest) {
        Point originalPoint = Point.fromLngLat(origin.longitude, origin.latitude);
        Point destPoint = Point.fromLngLat(dest.longitude, dest.latitude);
        double distanceInKM = TurfMeasurement.distance(originalPoint, destPoint);
        int exponent = calcExponent(distanceInKM);
        int zoomLevel = 13 - exponent;
        return Math.max(zoomLevel, 0) + 0.5;
    }

    private int calcExponent(double distanceInKM) {
        int ret = (int) Math.round(distanceInKM);
        int exponent = 0;
        while (ret >= 2) {
            ret = ret / 2;
            exponent++;
        }
        return exponent;
    }

    private void moveCamera(NextbillionMap map, NBLocation origin, NBLocation dest) {
        double zoomLevel = calcZoomLevel(origin, dest);
        NBLocation midLocation = calcMidLocation(origin, dest);
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(midLocation.latitude, midLocation.longitude))
                .zoom(zoomLevel)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 600);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Feature
    ///////////////////////////////////////////////////////////////////////////

    private FeatureCollection generateFeatureFromGeometry(String geometry) {
        final List<Feature> features = new ArrayList<>();
        LineString routeGeometry = LineString.fromPolyline(geometry, 5);
        Feature routeFeature = Feature.fromGeometry(routeGeometry);
        features.add(routeFeature);
        return FeatureCollection.fromFeatures(features);
    }

    private Feature buildFeatureFromLocation(NBLocation location) {
        return Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude));
    }

    private FeatureCollection generateFeatureCollectionFromPoints(List<NBLocation> points) {
        List<Feature> features = new ArrayList<>();
        for (NBLocation location : points) {
            features.add(buildFeatureFromLocation(location));
        }
        return FeatureCollection.fromFeatures(features);
    }

    private FeatureCollection generateFeatureCollectionFromLocation(NBLocation location) {
        return FeatureCollection.fromFeature(buildFeatureFromLocation(location));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////

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
