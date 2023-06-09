package ai.nextbillion;

import android.os.Bundle;

import java.net.URI;
import java.net.URISyntaxException;

import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.layers.CircleLayer;
import ai.nextbillion.maps.style.layers.HeatmapLayer;
import ai.nextbillion.maps.style.sources.GeoJsonSource;
import androidx.appcompat.app.AppCompatActivity;

import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.expressions.Expression.heatmapDensity;
import static ai.nextbillion.maps.style.expressions.Expression.interpolate;
import static ai.nextbillion.maps.style.expressions.Expression.linear;
import static ai.nextbillion.maps.style.expressions.Expression.literal;
import static ai.nextbillion.maps.style.expressions.Expression.rgb;
import static ai.nextbillion.maps.style.expressions.Expression.rgba;
import static ai.nextbillion.maps.style.expressions.Expression.stop;
import static ai.nextbillion.maps.style.expressions.Expression.zoom;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleOpacity;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleRadius;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleStrokeColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleStrokeWidth;
import static ai.nextbillion.maps.style.layers.PropertyFactory.heatmapColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.heatmapIntensity;
import static ai.nextbillion.maps.style.layers.PropertyFactory.heatmapOpacity;
import static ai.nextbillion.maps.style.layers.PropertyFactory.heatmapRadius;
import static ai.nextbillion.maps.style.layers.PropertyFactory.heatmapWeight;

/**
 * Test activity showcasing the heatmap layer api.
 */
public class HeatmapLayerActivity extends AppCompatActivity {

    private static final String EARTHQUAKE_SOURCE_URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";
    private static final String EARTHQUAKE_SOURCE_ID = "earthquakes";
    private static final String HEATMAP_LAYER_ID = "earthquakes-heat";
    private static final String HEATMAP_LAYER_SOURCE = "earthquakes";
    private static final String CIRCLE_LAYER_ID = "earthquakes-circle";

    private MapView mapView;
    private NextbillionMap nextbillionMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmaplayer);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            nextbillionMap = map;

            try {
                nextbillionMap.setStyle(new Style.Builder()
                        .fromUri("https://api.nextbillion.io/maps/dark/style.json")
                        .withSource(createEarthquakeSource())
                        .withLayerAbove(createHeatmapLayer(), "waterway-bridge")
                        .withLayerBelow(createCircleLayer(), HEATMAP_LAYER_ID)
                );
            } catch (URISyntaxException exception) {
            }
        });
    }

    private GeoJsonSource createEarthquakeSource() throws URISyntaxException {
        return new GeoJsonSource(EARTHQUAKE_SOURCE_ID, new URI(EARTHQUAKE_SOURCE_URL));
    }

    private HeatmapLayer createHeatmapLayer() {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, EARTHQUAKE_SOURCE_ID);
        layer.setMaxZoom(9);
        layer.setSourceLayer(HEATMAP_LAYER_SOURCE);
        layer.setProperties(

                // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
                // Begin color ramp at 0-stop with a 0-transparancy color
                // to create a blur-like effect.
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(33, 102, 172, 0),
                                literal(0.2), rgb(103, 169, 207),
                                literal(0.4), rgb(209, 229, 240),
                                literal(0.6), rgb(253, 219, 199),
                                literal(0.8), rgb(239, 138, 98),
                                literal(1), rgb(178, 24, 43)
                        )
                ),

                // Increase the heatmap weight based on frequency and property magnitude
                heatmapWeight(
                        interpolate(
                                linear(), get("mag"),
                                stop(0, 0),
                                stop(6, 1)
                        )
                ),

                // Increase the heatmap color weight weight by zoom level
                // heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 3)
                        )
                ),

                // Adjust the heatmap radius by zoom level
                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 20)
                        )
                ),

                // Transition from heatmap to circle layer by zoom level
                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 1),
                                stop(9, 0)
                        )
                )
        );
        return layer;
    }

    private CircleLayer createCircleLayer() {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, EARTHQUAKE_SOURCE_ID);
        circleLayer.setProperties(

                // Size circle radius by earthquake magnitude and zoom level
                circleRadius(
                        interpolate(
                                linear(), zoom(),
                                literal(7), interpolate(
                                        linear(), get("mag"),
                                        stop(1, 1),
                                        stop(6, 4)
                                ),
                                literal(16), interpolate(
                                        linear(), get("mag"),
                                        stop(1, 5),
                                        stop(6, 50)
                                )
                        )
                ),

                // Color circle by earthquake magnitude
                circleColor(
                        interpolate(
                                linear(), get("mag"),
                                literal(1), rgba(33, 102, 172, 0),
                                literal(2), rgb(103, 169, 207),
                                literal(3), rgb(209, 229, 240),
                                literal(4), rgb(253, 219, 199),
                                literal(5), rgb(239, 138, 98),
                                literal(6), rgb(178, 24, 43)
                        )
                ),

                // Transition from heatmap to circle layer by zoom level
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0),
                                stop(8, 1)
                        )
                ),
                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );

        return circleLayer;
    }

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
