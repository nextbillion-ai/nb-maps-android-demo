package ai.nextbillion.utils;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.kits.turf.TurfMeasurement;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonSource;

import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.layers.PropertyFactory.*;

/**
 * A movable icon backed by a GeoJsonSource and a SymbolLayer from the core Maps SDK.
 * Movement updates the feature's point geometry and bearing property in the source data.
 * Use on the map's UI thread and close before destroying the owning MapView.
 */
public final class MapSymbol implements AutoCloseable {
    public static final String ID_PROPERTY = "demo-symbol-id";
    private static final String BEARING_PROPERTY = "demo-symbol-bearing";
    private final String id;
    private final String sourceId;
    private final String layerId;
    private Style style;
    private GeoJsonSource source;
    private LatLng position;
    private ValueAnimator animator;
    private boolean closed;

    /**
     * The style must be loaded and contain the image referenced by icon.
     * Use a unique id for each active symbol because it determines the source and layer IDs.
     */
    public MapSymbol(Style style, String id, LatLng position, String icon,
                     float size, String anchor, String text) {
        this.id = id;
        this.style = style;
        this.position = new LatLng(position.getLatitude(), position.getLongitude());
        sourceId = id + "-source";
        layerId = id + "-layer";
        style.removeLayer(layerId);
        style.removeSource(sourceId);
        source = new GeoJsonSource(sourceId, feature(this.position, 0));
        style.addSource(source);
        String[] fonts = style.getSource("openmaptiles") != null || style.getSource("maptiler_planet") != null
                ? new String[]{"Roboto Medium", "Noto Sans Regular"} : new String[]{"Noto-Regular"};
        style.addLayer(new SymbolLayer(layerId, sourceId).withProperties(
                iconImage(icon), iconSize(size), iconAnchor(anchor),
                iconRotate(get(BEARING_PROPERTY)), iconAllowOverlap(true),
                textField(text == null ? "" : text), textFont(fonts), textAllowOverlap(true)));
    }

    private Feature feature(LatLng point, float bearing) {
        Feature feature = Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        feature.addStringProperty(ID_PROPERTY, id);
        feature.addNumberProperty(BEARING_PROPERTY, bearing);
        return feature;
    }

    // A reloaded style may reuse an ID for a different source. Check object identity
    // so an animator never updates a source that this symbol no longer owns.
    public boolean isAttached() {
        return !closed && style != null && style.isFullyLoaded()
                && style.getSource(sourceId) == source && style.getLayer(layerId) != null;
    }

    public void moveTo(LatLng target, long duration) {
        cancel();
        if (!isAttached()) { close(); return; }
        // LatLng is mutable; copy the endpoints so caller changes cannot alter this animation.
        LatLng start = new LatLng(position.getLatitude(), position.getLongitude());
        LatLng end = new LatLng(target.getLatitude(), target.getLongitude());
        float bearing = (float) TurfMeasurement.bearing(
                Point.fromLngLat(start.getLongitude(), start.getLatitude()),
                Point.fromLngLat(end.getLongitude(), end.getLatitude()));
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Math.max(0, duration));
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(value -> {
            // A style switch can happen mid-animation; release references to the old style.
            if (!isAttached()) {
                close();
                return;
            }
            float fraction = (float) value.getAnimatedValue();
            position = new LatLng(
                    start.getLatitude() + (end.getLatitude() - start.getLatitude()) * fraction,
                    start.getLongitude() + (end.getLongitude() - start.getLongitude()) * fraction);
            source.setGeoJson(feature(position, bearing));
        });
        animator.start();
    }

    /** Stops movement while keeping the symbol available for the next moveTo call. */
    public void cancel() {
        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
        }
    }

    /** Releases animation listeners and owned map objects. This instance cannot be reused. */
    @Override
    public void close() {
        cancel();
        if (isAttached()) {
            // Layers reference their source, so remove the layer before removing its source.
            style.removeLayer(layerId);
            style.removeSource(sourceId);
        }
        closed = true;
        source = null;
        style = null;
    }
}
