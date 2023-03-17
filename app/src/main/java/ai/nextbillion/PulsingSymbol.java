package ai.nextbillion;

import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleOpacity;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circlePitchAlignment;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleRadius;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconAnchor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconOffset;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconSize;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.NbmapAnimator;
import ai.nextbillion.maps.location.PulsingLocationCircleAnimator;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.layers.CircleLayer;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.Property;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.style.sources.GeoJsonSource;


public class PulsingSymbol {
    static final String PULSING_RADIUS = "pulsing-circle-radius";
    static final String PULSING_OPACITY = "pulsing-circle-opacity";
    private Layer symbolLayer;
    private Layer circleLayer;
    private GeoJsonSource locationSource;
    private Feature locationFeature;

    PulsingLocationCircleAnimator pulsingLocationCircleAnimator;

    private String symbolId;
    private final String locationSourceId;
    private final String pulsingCircleLayerId;
    private final String symbolLayerId;

    public PulsingSymbol(String symbolId) {
        this.symbolId = symbolId;
        pulsingCircleLayerId = symbolId + "-pulsing-circle-layer";
        locationSourceId = symbolId + "-location-source";
        symbolLayerId = symbolId + "-symbol-layer";
    }

    public void init(Style style, Bitmap icon, String iconId) {
        style.addImage(iconId, icon);
        GeoJsonOptions jsonOptions = new GeoJsonOptions().withMaxZoom(16);
        locationSource = new GeoJsonSource(locationSourceId, jsonOptions);
        style.removeLayer(symbolLayerId);
        style.removeLayer(pulsingCircleLayerId);
        style.removeSource(locationSourceId);
        style.addSource(locationSource);
        symbolLayer = new SymbolLayer(symbolLayerId, locationSourceId).withProperties(
                iconImage(iconId),
                iconAnchor(ICON_ANCHOR_BOTTOM),
                iconOffset(new Float[]{1f, 15f}),
                iconSize(0.5f)
        );
        style.addLayer(symbolLayer);
        circleLayer = new CircleLayer(pulsingCircleLayerId, locationSourceId)
                .withProperties(
                        circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                        circleRadius(get(PULSING_RADIUS)),
                        circleColor("#00ff00"),
                        circleOpacity(get(PULSING_OPACITY))
                );
        style.addLayer(circleLayer);
    }

    public void update(LatLng latLng) {
        locationFeature = Feature.fromGeometry(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        locationFeature.addNumberProperty(PULSING_RADIUS, 30);
        locationFeature.addNumberProperty(PULSING_OPACITY, 0.2);
        locationSource.setGeoJson(locationFeature);
//        startPulsing(pulsingCircleRadiusListener, 100, 2000, 50, new AccelerateDecelerateInterpolator());
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public void startPulsing(NbmapAnimator.AnimationsValueChangeListener listener, int maxAnimationFps,
                             float pulseSingleDuration,
                             float pulseMaxRadius,
                             Interpolator pulseInterpolator) {
        if (pulsingLocationCircleAnimator == null) {
            pulsingLocationCircleAnimator =
                    new PulsingLocationCircleAnimator(listener, maxAnimationFps, pulseMaxRadius);
            pulsingLocationCircleAnimator.setDuration((long) pulseSingleDuration);
            pulsingLocationCircleAnimator.setRepeatMode(ValueAnimator.RESTART);
            pulsingLocationCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
            pulsingLocationCircleAnimator.setInterpolator(pulseInterpolator);
        } else {
            pulsingLocationCircleAnimator.removeAllUpdateListeners();
            pulsingLocationCircleAnimator.removeAllListeners();
            pulsingLocationCircleAnimator.cancel();
        }
        pulsingLocationCircleAnimator.start();
    }

    private final NbmapAnimator.AnimationsValueChangeListener<Float> pulsingCircleRadiusListener = newPulseRadiusValue -> {
        Float newPulseOpacityValue = (float) 1 - ((newPulseRadiusValue / 100) * 3);
        updatePulsingUi(newPulseRadiusValue, newPulseOpacityValue);
    };

    public void updatePulsingUi(float radius, @Nullable Float opacity) {
        locationFeature.addNumberProperty(PULSING_RADIUS, radius);
        if (opacity != null) {
            locationFeature.addNumberProperty(PULSING_OPACITY, opacity);
        }
        locationSource.setGeoJson(locationFeature);
    }
}
