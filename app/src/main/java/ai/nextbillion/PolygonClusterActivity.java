package ai.nextbillion;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.URI;
import java.net.URISyntaxException;

import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.style.expressions.Expression;
import ai.nextbillion.maps.style.layers.CircleLayer;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.style.sources.GeoJsonSource;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import static ai.nextbillion.maps.style.expressions.Expression.all;
import static ai.nextbillion.maps.style.expressions.Expression.get;
import static ai.nextbillion.maps.style.expressions.Expression.gt;
import static ai.nextbillion.maps.style.expressions.Expression.gte;
import static ai.nextbillion.maps.style.expressions.Expression.has;
import static ai.nextbillion.maps.style.expressions.Expression.literal;
import static ai.nextbillion.maps.style.expressions.Expression.lt;
import static ai.nextbillion.maps.style.expressions.Expression.toNumber;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.circleRadius;
import static ai.nextbillion.maps.style.layers.PropertyFactory.iconImage;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textAllowOverlap;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textField;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textIgnorePlacement;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textSize;

public class PolygonClusterActivity extends AppCompatActivity implements OnMapReadyCallback, NextbillionMap.OnMapClickListener, View.OnClickListener {

    private static final String TAG = "PolygonClusterActivity";
    public static final String SOURCE_ID = "bus_stop";
    public static final String SOURCE_ID_CLUSTER = "bus_stop_cluster";
    public static final String URL_BUS_ROUTES = "https://raw.githubusercontent.com/cheeaun/busrouter-sg/master/data/2/bus-stops.geojson";
    public static final String LAYER_ID = "stops_layer";
    private static final String TAXI = "taxi";
    private ImageView backBtn;
    private MapView mapView;
    private NextbillionMap nextbillionMap;

    private FloatingActionButton styleFab;
    private FloatingActionButton routeFab;

    private CircleLayer layer;
    private GeoJsonSource source;

    private int currentStyleIndex = 0;
    private boolean isLoadingStyle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon_cluster);
        mapView = findViewById(R.id.map_view);
        backBtn = findViewById(R.id.back_ib);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nbMap) {
        this.nextbillionMap = nbMap;
        mapView.addOnDidFinishLoadingStyleListener(() -> {
            Style style = nextbillionMap.getStyle();
            style.addImage(TAXI,(BitmapDrawable)getResources().getDrawable(R.mipmap.ic_nb_taxi));
            addBusStopSource(style);
            addBusStopCircleLayer(style);
            initFloatingActionButtons();
            isLoadingStyle = false;
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {

        return false;
    }


    private void addBusStopSource(Style style) {
        try {
            source = new GeoJsonSource(SOURCE_ID, new URI(URL_BUS_ROUTES));
        } catch (URISyntaxException exception) {
            Log.e(TAG, "This is not an url... ");
        }
        style.addSource(source);
    }

    private void addBusStopCircleLayer(Style style) {
        layer = new CircleLayer(LAYER_ID, SOURCE_ID);
        layer.setProperties(
                circleColor(Color.parseColor("#FF0000")),
                circleRadius(2.0f)
        );
        style.addLayer(layer);
    }

    private void initFloatingActionButtons() {
        routeFab = findViewById(R.id.fab_route);
        routeFab.setColorFilter(ContextCompat.getColor(PolygonClusterActivity.this, R.color.purple_200));
        routeFab.setOnClickListener(PolygonClusterActivity.this);

        styleFab = findViewById(R.id.fab_style);
        styleFab.setOnClickListener(PolygonClusterActivity.this);
    }

    @Override
    public void onClick(View view) {
        if (isLoadingStyle) {
            return;
        }

        if (view.getId() == R.id.fab_route) {
            showBusCluster();
        } else if (view.getId() == R.id.fab_style) {
            changeMapStyle();
        }
    }

    private void showBusCluster() {
        removeFabs();
        removeOldSource();
        addClusteredSource();
    }

    private void removeOldSource() {
        nextbillionMap.getStyle().removeSource(SOURCE_ID);
        nextbillionMap.getStyle().removeLayer(LAYER_ID);
    }

    private void addClusteredSource() {
        try {
            nextbillionMap.getStyle().addSource(
                    new GeoJsonSource(SOURCE_ID_CLUSTER,
                            new URI(URL_BUS_ROUTES),
                            new GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(14)
                                    .withClusterRadius(50)
                    )
            );
        } catch (URISyntaxException malformedUrlException) {
            Log.e(TAG, "This is not an url... ");
        }

        // Add unclustered layer
        int[][] layers = new int[][]{
                new int[]{150, ResourcesCompat.getColor(getResources(), R.color.purple_200, getTheme())},
                new int[]{20, ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme())},
                new int[]{0, ResourcesCompat.getColor(getResources(), R.color.color_4158ce, getTheme())}
        };

        SymbolLayer unclustered = new SymbolLayer("unclustered-points", SOURCE_ID_CLUSTER);
        unclustered.setProperties(
                iconImage(TAXI)
        );

        nextbillionMap.getStyle().addLayer(unclustered);

        for (int i = 0; i < layers.length; i++) {
            // Add some nice circles
            CircleLayer circles = new CircleLayer("cluster-" + i, SOURCE_ID_CLUSTER);
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f)
            );

            Expression pointCount = toNumber(get("point_count"));
            circles.setFilter(
                    i == 0
                            ? all(has("point_count"),
                            gte(pointCount, literal(layers[i][0]))
                    ) : all(has("point_count"),
                            gt(pointCount, literal(layers[i][0])),
                            lt(pointCount, literal(layers[i - 1][0]))
                    )
            );
            nextbillionMap.getStyle().addLayer(circles);
        }

        // Add the count labels
        SymbolLayer count = new SymbolLayer("count", SOURCE_ID_CLUSTER);
        count.setProperties(
                textField(Expression.toString(get("point_count"))),
                textSize(12f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
                textAllowOverlap(true)
        );
        nextbillionMap.getStyle().addLayer(count);
    }

    private void removeFabs() {
        routeFab.setVisibility(View.GONE);
        styleFab.setVisibility(View.GONE);
    }

    private void changeMapStyle() {
        isLoadingStyle = true;
        removeBusStop();
        loadNewStyle();
    }

    private void removeBusStop() {
        nextbillionMap.getStyle().removeLayer(layer);
        nextbillionMap.getStyle().removeSource(source);
    }

    private void loadNewStyle() {
        nextbillionMap.setStyle(new Style.Builder().fromUri(getNextStyle()));
    }

    private void addBusStop() {
        nextbillionMap.getStyle().addLayer(layer);
        nextbillionMap.getStyle().addSource(source);
    }

    private String getNextStyle() {
        currentStyleIndex++;
        if (currentStyleIndex == Data.STYLES.length) {
            currentStyleIndex = 0;
        }
        return Data.STYLES[currentStyleIndex];
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
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private static class Data {
        private static final String[] STYLES = new String[]{
                "https://api.nextbillion.io/maps/streets/style.json",
                "https://api.nextbillion.io/maps/hybrid/style.json",
                "https://api.nextbillion.io/maps/dark/style.json"
        };
    }
}