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
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.layers.CircleLayer;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.Property;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.style.sources.GeoJsonSource;


/**
 * Draws an icon and a pulsing circle from a shared GeoJSON point using public SDK layers.
 * Call init, then update with a position, then startPulsing on the map's UI thread.
 * Stop pulsing when the screen stops and close before destroying its MapView.
 */
public class PulsingSymbol implements AutoCloseable {
    static final String PULSING_RADIUS = "pulsing-circle-radius";
    static final String PULSING_OPACITY = "pulsing-circle-opacity";
    private Style ownerStyle;
    private Layer symbolLayer;
    private Layer circleLayer;
    private GeoJsonSource locationSource;
    private Feature locationFeature;

    private ValueAnimator pulsingLocationCircleAnimator;

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

    /** Attaches to a loaded style; also releases resources from an earlier initialization. */
    public void init(Style style, Bitmap icon, String iconId) {
        close();
        ownerStyle = style;
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
                        // Each animation update writes these feature properties to the shared source.
                        circleRadius(get(PULSING_RADIUS)),
                        circleColor("#00ff00"),
                        circleOpacity(get(PULSING_OPACITY))
                );
        style.addLayer(circleLayer);
    }

    public void update(LatLng latLng) {
        if (!isAttached()) return;
        locationFeature = Feature.fromGeometry(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        locationFeature.addNumberProperty(PULSING_RADIUS, 30);
        locationFeature.addNumberProperty(PULSING_OPACITY, 0.2);
        locationSource.setGeoJson(locationFeature);
    }

    /** A null listener uses the default expanding and fading circle animation. */
    public void startPulsing(ValueAnimator.AnimatorUpdateListener listener, int maxAnimationFps,
                             float pulseSingleDuration, float pulseMaxRadius,
                             Interpolator pulseInterpolator) {
        stopPulsing();
        if (!isAttached() || locationFeature == null) return;
        ValueAnimator.AnimatorUpdateListener callback = listener != null ? listener : pulsingCircleRadiusListener;
        pulsingLocationCircleAnimator = ValueAnimator.ofFloat(0f, Math.max(0f, pulseMaxRadius));
        pulsingLocationCircleAnimator.setDuration(Math.max(1L, (long) pulseSingleDuration));
        pulsingLocationCircleAnimator.setRepeatMode(ValueAnimator.RESTART);
        pulsingLocationCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulsingLocationCircleAnimator.setInterpolator(pulseInterpolator);
        // Throttle source updates to avoid uploading GeoJSON on every display frame.
        final long interval = 1000L / Math.max(1, maxAnimationFps);
        final long[] lastFrame = {0};
        pulsingLocationCircleAnimator.addUpdateListener(animation -> {
            if (!isAttached()) {
                close();
                return;
            }
            long now = android.os.SystemClock.uptimeMillis();
            if (now - lastFrame[0] >= interval) {
                lastFrame[0] = now;
                callback.onAnimationUpdate(animation);
            }
        });
        pulsingLocationCircleAnimator.start();
    }

    private final ValueAnimator.AnimatorUpdateListener pulsingCircleRadiusListener = animation -> {
        float radius = (float) animation.getAnimatedValue();
        updatePulsingUi(radius, 1f - (radius / 100f) * 3f);
    };

    public void updatePulsingUi(float radius, @Nullable Float opacity) {
        if (!isAttached() || locationFeature == null) {
            stopPulsing();
            return;
        }
        locationFeature.addNumberProperty(PULSING_RADIUS, radius);
        if (opacity != null) {
            locationFeature.addNumberProperty(PULSING_OPACITY, Math.max(0f, Math.min(1f, opacity)));
        }
        locationSource.setGeoJson(locationFeature);
    }
    // Source identity detects a style replacement even if the replacement reuses our ID.
    private boolean isAttached() {
        return ownerStyle != null && ownerStyle.isFullyLoaded()
                && ownerStyle.getSource(locationSourceId) == locationSource;
    }

    /** Stops the animation while keeping the point and layers available for reuse. */
    public void stopPulsing() {
        if (pulsingLocationCircleAnimator != null) {
            pulsingLocationCircleAnimator.removeAllUpdateListeners();
            pulsingLocationCircleAnimator.cancel();
            pulsingLocationCircleAnimator = null;
        }
    }

    /** Stops animation, removes owned layers/source, and releases references to the style. */
    @Override
    public void close() {
        stopPulsing();
        if (isAttached()) {
            ownerStyle.removeLayer(symbolLayerId);
            ownerStyle.removeLayer(pulsingCircleLayerId);
            ownerStyle.removeSource(locationSourceId);
        }
        ownerStyle = null;
        locationSource = null;
        locationFeature = null;
        symbolLayer = null;
        circleLayer = null;
    }

}
