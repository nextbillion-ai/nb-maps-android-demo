package ai.nextbillion;

import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconAllowOverlap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconIgnorePlacement;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconRotate;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.view.animation.LinearInterpolator;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonSource;
import ai.nextbillion.kits.turf.TurfMeasurement;

import java.util.List;

public class VehicleSymbol {
    private static final String VEHICLE_LAYER_ID = "Vehicle-layer-Icon";
    private static final String VEHICLE_SOURCE_ID = "Vehicle-source-Icon";
    private static final String VEHICLE_ICON_ID = "Vehicle-symbol-Icon";
    private static final String VEHICLE_BEARING = "Vehicle-bearing";

    private Feature feature;
    private LatLng current;
    private GeoJsonSource vehicleSource;
    private ValueAnimator valueAnimator;
    private long duration = 5000;

    public VehicleSymbol(Style style, LatLng latLng, Bitmap icon, long duration) {
        if (duration > this.duration) {
            this.duration = duration;
        }
        current = latLng;
        feature = Feature.fromGeometry(Point.fromLngLat(
                current.getLongitude(),
                current.getLatitude())
        );
        setIcon(icon, style);
        feature.properties().addProperty(VEHICLE_BEARING, 0);
        initLayer(style);
    }

    private void initLayer(Style style) {
        vehicleSource = new GeoJsonSource(VEHICLE_SOURCE_ID, feature);
        style.addSource(vehicleSource);

        SymbolLayer symbolLayer = new SymbolLayer(VEHICLE_LAYER_ID, VEHICLE_SOURCE_ID);
        style.addLayer(symbolLayer);
        symbolLayer.withProperties(
                iconImage(VEHICLE_ICON_ID),
                iconAllowOverlap(true),
                iconRotate(get(VEHICLE_BEARING)),
                iconIgnorePlacement(true));
    }

    private void setIcon(Bitmap bitmap, Style style) {
        style.addImage(VEHICLE_ICON_ID, bitmap);
    }


    public void onLocationUpdated(LatLng latLng) {
        float newBearing = getBearing(current, latLng);
        current = latLng;
        feature = Feature.fromGeometry(Point.fromLngLat(
                current.getLongitude(),
                current.getLatitude())
        );
        feature.properties().addProperty(VEHICLE_BEARING, newBearing);
        vehicleSource.setGeoJson(feature);
    }

    public void animateAlongRoute(List<LatLng> route) {
        initAnimator(route);
        valueAnimator.start();
    }

    private void initAnimator(List<LatLng> route) {
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            valueAnimator.cancel();
        }

        valueAnimator = ValueAnimator.ofObject(new LatLngEvaluator(), route.get(0), route.get(route.size() - 1));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LatLng latLng = (LatLng) animation.getAnimatedValue();
                onLocationUpdated(latLng);
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(duration);
    }

    public void onDestroy() {
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            valueAnimator.cancel();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private static float getBearing(LatLng from, LatLng to) {
        return (float) TurfMeasurement.bearing(
                Point.fromLngLat(from.getLongitude(), from.getLatitude()),
                Point.fromLngLat(to.getLongitude(), to.getLatitude())
        );
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }
}
