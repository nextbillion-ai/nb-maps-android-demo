package ai.nextbillion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.FeatureCollection;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.style.expressions.Expression;
import ai.nextbillion.maps.style.layers.FillLayer;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonSource;
import ai.nextbillion.utils.ResourceUtils;
import androidx.appcompat.app.AppCompatActivity;

import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.expressions.Expression.literal;
import static ai.nextbillion.maps.style.expressions.Expression.lt;
import static ai.nextbillion.maps.style.expressions.Expression.toNumber;
import static ai.nextbillion.maps.style.layers.PropertyFactory.fillColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;

/**
 * Demo's query rendered features
 */
public class QueryFeatureAndSymbolActivity extends AppCompatActivity {

    public MapView mapView;
    private NextbillionMap nextbillionMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_feature_and_symbol);

        final View selectionBox = findViewById(R.id.selection_box);

        // Initialize map as normal
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(nextbillionMap -> {
            QueryFeatureAndSymbolActivity.this.nextbillionMap = nextbillionMap;

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(14.589503119868022, 120.98188196701062))
                    .zoom(16)
                    .bearing(0)
                    .tilt(30)
                    .build();
            nextbillionMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

            // Add layer / source
            final GeoJsonSource source = new GeoJsonSource("highlighted-shapes-source");
            final Layer layer = new FillLayer("highlighted-shapes-layer", "highlighted-shapes-source")
                    .withProperties(fillColor(Color.YELLOW));

            selectionBox.setOnClickListener(view -> {
                // Query
                int top = selectionBox.getTop() - mapView.getTop();
                int left = selectionBox.getLeft() - mapView.getLeft();
                RectF box = new RectF(left, top, left + selectionBox.getWidth(), top + selectionBox.getHeight());

                Expression filter = lt(toNumber(get("height")), literal(10));
                List<Feature> features = nextbillionMap.queryRenderedFeatures(box, filter, "building");
                List<Feature> symbols = nextbillionMap.queryRenderedFeatures(box, "symbols-layer");

                // Show count
                Toast.makeText(
                        QueryFeatureAndSymbolActivity.this,
                        String.format("%s buildings in box\n%s symbols in box", features.size(), symbols.size()),
                        Toast.LENGTH_SHORT).show();

                // Update source data
                source.setGeoJson(FeatureCollection.fromFeatures(features));
            });

            nextbillionMap.setStyle(new Style.Builder()
                            .fromUri(StyleConstants.NBMAP_STREETS)
                            .withSource(source)
                            .withLayer(layer)
                    , style -> addSymbolLayer(style));

        });
    }

    public void addSymbolLayer(Style style) {
        try {
            String testPoints = ResourceUtils.readRawResource(mapView.getContext(), R.raw.test_points_utrecht);
            Bitmap markerImage = BitmapFactory.decodeResource(getResources(), ai.nextbillion.maps.R.drawable.nbmap_marker_icon_default);

            style.addImage("test-icon", markerImage);
            style.addSource(new GeoJsonSource("symbols-source", testPoints));
            style.addLayer(new SymbolLayer("symbols-layer", "symbols-source")
                    .withProperties(
                            iconImage("test-icon")
                    ));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public NextbillionMap getNextbillionMap() {
        return nextbillionMap;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
