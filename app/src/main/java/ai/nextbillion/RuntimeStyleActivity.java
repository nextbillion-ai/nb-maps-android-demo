package ai.nextbillion;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.kits.geojson.Polygon;
import ai.nextbillion.maps.annotations.PolygonOptions;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.style.layers.FillLayer;
import ai.nextbillion.maps.style.layers.Layer;
import ai.nextbillion.maps.style.layers.SymbolLayer;
import ai.nextbillion.maps.style.layers.TransitionOptions;
import ai.nextbillion.maps.style.sources.Source;
import androidx.appcompat.app.AppCompatActivity;

import static ai.nextbillion.maps.style.expressions.Expression.color;
import static ai.nextbillion.maps.style.expressions.Expression.exponential;
import static ai.nextbillion.maps.style.expressions.Expression.interpolate;
import static ai.nextbillion.maps.style.expressions.Expression.literal;
import static ai.nextbillion.maps.style.expressions.Expression.stop;
import static ai.nextbillion.maps.style.expressions.Expression.switchCase;
import static ai.nextbillion.maps.style.expressions.Expression.within;
import static ai.nextbillion.maps.style.expressions.Expression.zoom;
import static ai.nextbillion.maps.style.layers.Property.NONE;
import static ai.nextbillion.maps.style.layers.Property.VISIBLE;
import static ai.nextbillion.maps.style.layers.PropertyFactory.backgroundOpacity;
import static ai.nextbillion.maps.style.layers.PropertyFactory.fillColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textColor;
import static ai.nextbillion.maps.style.layers.PropertyFactory.textOpacity;
import static ai.nextbillion.maps.style.layers.PropertyFactory.visibility;

/**
 * Test activity showcasing the runtime style API.
 */
public class RuntimeStyleActivity extends AppCompatActivity {

    private MapView mapView;
    private NextbillionMap nextbillionMap;
    private boolean styleLoaded;

    List<List<Point>> lngLats = Collections.singletonList(
            Arrays.asList(
                    Point.fromLngLat(-15.468749999999998,
                            41.77131167976407),
                    Point.fromLngLat(15.468749999999998,
                            41.77131167976407),
                    Point.fromLngLat(15.468749999999998,
                            58.26328705248601),
                    Point.fromLngLat(-15.468749999999998,
                            58.26328705248601),
                    Point.fromLngLat(-15.468749999999998,
                            41.77131167976407)
            )
    );

    Polygon polygon = Polygon.fromLngLats(lngLats);

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runtime_style);
        mHandler = new Handler(getMainLooper());

        // Initialize map as normal
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {
            // Store for later
            nextbillionMap = map;

            // Center and Zoom (Amsterdam, zoomed to streets)
            nextbillionMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.379189, 4.899431), 1));
            nextbillionMap.setStyle(
                    new Style.Builder()
                            .fromUri(StyleConstants.NBMAP_STREETS)
                            // set custom transition
                            .withTransition(new TransitionOptions(250, 50)), style -> {
                        styleLoaded = true;
                        SymbolLayer label = (SymbolLayer) style.getLayer("country_2");
                        label.setProperties(
                                textOpacity(switchCase(within(polygon), literal(1.0f), literal(0.5f))),
                                textColor(Color.RED)
                        );
                    }
            );

            nextbillionMap.addPolygon(new PolygonOptions()
                    .add(new LatLng(41.77131167976407,-15.468749999999998))
                    .add(new LatLng(41.77131167976407,15.468749999999998))
                    .add(new LatLng(58.26328705248601,15.468749999999998))
                    .add(new LatLng(58.26328705248601,-15.468749999999998))
                    .add(new LatLng(41.77131167976407,-15.468749999999998))
                    .fillColor(0x050000ff));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_runtime_style, menu);
        return true;
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
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!styleLoaded) {
            return false;
        }

        try {
            if (item.getItemId() == R.id.action_list_layers) {
                listLayers();
                return true;
            } else if (item.getItemId() == R.id.action_list_sources) {
                listSources();
                return true;
            } else if (item.getItemId() == R.id.action_water_color) {
                setWaterColor();
                return true;
            } else if (item.getItemId() == R.id.action_background_opacity) {
                setBackgroundOpacity();
                return true;
            } else if (item.getItemId() == R.id.action_layer_visibility) {
                setLayerInvisible();
                return true;
            } else if (item.getItemId() == R.id.action_update_water_color_on_zoom) {
                updateWaterColorOnZoom();
                return true;
            } else if (item.getItemId() == R.id.action_bring_water_to_front) {
                bringWaterToFront();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void listLayers() {
        List<Layer> layers = nextbillionMap.getStyle().getLayers();
        StringBuilder builder = new StringBuilder("Layers:");
        for (Layer layer : layers) {
            builder.append("\n");
            builder.append(layer.getId());
        }
        Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show();
    }

    private void listSources() {
        List<Source> sources = nextbillionMap.getStyle().getSources();
        StringBuilder builder = new StringBuilder("Sources:");
        for (Source source : sources) {
            builder.append("\n");
            builder.append(source.getId());
        }
        Toast.makeText(this, builder.toString(), Toast.LENGTH_LONG).show();
    }

    private void setLayerInvisible() {
        String[] roadLayers = new String[]{"water"};
        for (String roadLayer : roadLayers) {
            Layer layer = nextbillionMap.getStyle().getLayer(roadLayer);
            if (layer != null) {
                layer.setProperties(visibility(NONE));
            }
        }
    }

    private void setBackgroundOpacity() {
        Layer background = nextbillionMap.getStyle().getLayer("background");
        if (background != null) {
            background.setProperties(backgroundOpacity(0.2f));
        }
    }

    private void setWaterColor() {
        FillLayer water = nextbillionMap.getStyle().getLayerAs("water");
        if (water != null) {
            water.setFillColorTransition(new TransitionOptions(7500, 1000));
            water.setProperties(
                    visibility(VISIBLE),
                    fillColor(Color.RED)
            );
        } else {
            Toast.makeText(RuntimeStyleActivity.this, "No water layer in this style", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateWaterColorOnZoom() {
        FillLayer layer = nextbillionMap.getStyle().getLayerAs("water");
        if (layer == null) {
            return;
        }

        // Set a zoom function to update the color of the water
        layer.setProperties(
                fillColor(
                        interpolate(
                                exponential(0.8f),
                                zoom(),
                                stop(1, color(Color.GREEN)),
                                stop(4, color(Color.BLUE)),
                                stop(12, color(Color.RED)),
                                stop(20, color(Color.BLACK))
                        )
                )
        );

        // do some animations to show it off properly
        nextbillionMap.animateCamera(CameraUpdateFactory.zoomTo(1), 1500);
    }


    private void bringWaterToFront() {
        Layer water = nextbillionMap.getStyle().getLayer("water");
        if (water != null) {
            nextbillionMap.getStyle().removeLayer(water);
            nextbillionMap.getStyle().addLayerAt(water, nextbillionMap.getStyle().getLayers().size() - 1);
        } else {
            Toast.makeText(this, "No water layer in this style", Toast.LENGTH_SHORT).show();
        }
    }
}
