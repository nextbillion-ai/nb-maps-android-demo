package ai.nextbillion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.FeatureCollection;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.kits.turf.TurfMeasurement;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.geometry.LatLngBounds;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonSource;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconAllowOverlap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconIgnorePlacement;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconRotate;

public class AnimateMarkersActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAXI = "taxi";
    private static final String TAXI_LAYER = "taxi-layer";
    private static final String TAXI_SOURCE = "taxi-source";
    private static final String PROPERTY_BEARING = "bearing";
    private static final int DURATION_RANDOM_MAX = 1500;
    private static final int DURATION_BASE = 3000;
    private final Random random = new Random();

    private MapView mapView;
    private NextbillionMap nextbillionMap;
    private Style style;
    private List<Taxi> taxis = new ArrayList<>();
    private GeoJsonSource taxiSource;
    private List<Animator> animators = new ArrayList<>();
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animate_markers);
        ivBack = findViewById(R.id.iv_back);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        this.nextbillionMap = nextbillionMap;
        nextbillionMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                AnimateMarkersActivity.this.style = style;
                generateTaxis();
                animateTaxis();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Animator animator : animators) {
            if (animator != null) {
                animator.removeAllListeners();
                animator.cancel();
            }
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void generateTaxis(){
        style.addImage(TAXI,
                ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_nb_taxi)).getBitmap());

        for (int i = 0; i < 10; i++) {
            LatLng latLng = getRandomLatLng();
            LatLng destination = getRandomLatLng();
            JsonObject properties = new JsonObject();

            properties.addProperty(PROPERTY_BEARING, Taxi.getBearing(latLng, destination));
            Feature feature = Feature.fromGeometry(
                    Point.fromLngLat(
                            latLng.getLongitude(),
                            latLng.getLatitude()), properties);

            Taxi taxi = new Taxi(feature, destination, getDuration());
            taxis.add(taxi);
        }

        taxiSource = new GeoJsonSource(TAXI_SOURCE, taxiMarkerFeatures());
        style.addSource(taxiSource);

        SymbolLayer symbolLayer = new SymbolLayer(TAXI_LAYER, TAXI_SOURCE);
        style.addLayer(symbolLayer);
        symbolLayer.withProperties(
                iconImage(TAXI),
                iconAllowOverlap(true),
                iconRotate(get(PROPERTY_BEARING)),
                iconIgnorePlacement(true)
        );
    }

    private FeatureCollection taxiMarkerFeatures() {
        List<Feature> features = new ArrayList<>();
        for (Taxi taxi : taxis) {
            features.add(taxi.feature);
        }
        return FeatureCollection.fromFeatures(features);
    }

    private void animateTaxis(){
        final Taxi longestDrive = getLongestDrive();
        final Random random = new Random();
        for (final Taxi taxi : taxis) {
            final boolean isLongestDrive = longestDrive.equals(taxi);
            ValueAnimator valueAnimator = ValueAnimator.ofObject(new LatLngEvaluator(), taxi.current, taxi.next);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private LatLng latLng;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    latLng = (LatLng) animation.getAnimatedValue();
                    taxi.current = latLng;
                    if (isLongestDrive) {
                        updateTaxisSource();;
                    }
                }
            });

            if (isLongestDrive) {
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        updateDestinations();
                        animateTaxis();
                    }
                });
            }

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    taxi.feature.properties().addProperty("bearing", Taxi.getBearing(taxi.current, taxi.next));
                }
            });

            int offset = random.nextInt(2) == 0 ? 0 : random.nextInt(1000) + 250;
            valueAnimator.setStartDelay(offset);
            valueAnimator.setDuration(taxi.duration - offset);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.start();

            animators.add(valueAnimator);
        }
    }

    private void updateTaxisSource() {
        for (Taxi taxi : taxis) {
            taxi.updateFeature();
        }
        taxiSource.setGeoJson(taxiMarkerFeatures());
    }

    private void updateDestinations(){
        for (Taxi taxi : taxis) {
            taxi.setNext(getRandomLatLng());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private LatLng getRandomLatLng() {
        LatLngBounds bounds = nextbillionMap.getProjection().getVisibleRegion().latLngBounds;
        Random generator = new Random();
        double randomLat = bounds.getLatSouth() + generator.nextDouble()
                * (bounds.getLatNorth() - bounds.getLatSouth());
        double randomLon = bounds.getLonWest() + generator.nextDouble()
                * (bounds.getLonEast() - bounds.getLonWest());
        return new LatLng(randomLat, randomLon);
    }

    private long getDuration() {
        return random.nextInt(DURATION_RANDOM_MAX) + DURATION_BASE;
    }

    private Taxi getLongestDrive() {
        Taxi longestDrive = null;
        for (Taxi taxi : taxis) {
            if (longestDrive == null) {
                longestDrive = taxi;
            } else if (longestDrive.duration < taxi.duration) {
                longestDrive = taxi;
            }
        }
        return longestDrive;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private static class Taxi {
        private Feature feature;
        private LatLng next;
        private LatLng current;
        private long duration;

        Taxi(Feature feature, LatLng next, long duration) {
            this.feature = feature;
            Point point = ((Point) feature.geometry());
            this.current = new LatLng(point.latitude(), point.longitude());
            this.duration = duration;
            this.next = next;
        }

        void setNext(LatLng next) {
            this.next = next;
        }

        void updateFeature() {
            feature = Feature.fromGeometry(Point.fromLngLat(
                    current.getLongitude(),
                    current.getLatitude())
            );
            feature.properties().addProperty("bearing", getBearing(current, next));
        }

        private static float getBearing(LatLng from, LatLng to) {
            return (float) TurfMeasurement.bearing(
                    Point.fromLngLat(from.getLongitude(), from.getLatitude()),
                    Point.fromLngLat(to.getLongitude(), to.getLatitude())
            );
        }
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
