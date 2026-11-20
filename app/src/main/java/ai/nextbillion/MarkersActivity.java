package ai.nextbillion;

import android.app.ProgressDialog;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.nextbillion.maps.annotations.MarkerOptions;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.utils.GeoParseUtil;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Test activity showcasing adding a large amount of Markers.
 */
public class MarkersActivity extends AppCompatActivity {

    private NextbillionMap nextbillionMap;
    private boolean destroyed;
    private LoadLocationTask loadLocationTask;
    private MapView mapView;
    private List<LatLng> locations;
    private ProgressDialog progressDialog;
    private static final DecimalFormat LAT_LON_FORMATTER = new DecimalFormat("#.#####");
    List<MarkerOptions> markerOptionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::initMap);
    }


    private void initMap(NextbillionMap nextbillionMap) {
        if (destroyed) return;
        this.nextbillionMap =  nextbillionMap;
        nextbillionMap.setStyle(StyleConstants.getBright());
        if (locations == null) {
            progressDialog = ProgressDialog.show(this, "Loading", "Fetching markers", false);
            loadLocationTask = new LoadLocationTask(this, 100);
            loadLocationTask.execute();
        } else {
            showMarkers(100);
        }
        nextbillionMap.addOnMapLongClickListener(point -> {
            addMarker(point);
            return false;
        });

        nextbillionMap.addOnMapClickListener(point -> {
            addMarker(point);
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_press_for_marker, menu);
        return true;
    }

    private void addMarker(LatLng point) {
        final PointF pixel = nextbillionMap.getProjection().toScreenLocation(point);

        String title = LAT_LON_FORMATTER.format(point.getLatitude()) + ", "
                + LAT_LON_FORMATTER.format(point.getLongitude());
        String snippet = "X = " + (int) pixel.x + ", Y = " + (int) pixel.y;

        MarkerOptions marker = new MarkerOptions()
                .position(point)
                .title(title)
                .snippet(snippet);

        markerOptionsList.add(marker);
        nextbillionMap.addMarker(marker);

    }

    private void onLatLngListLoaded(List<LatLng> latLngs, int amount) {
        if (destroyed) return;
        if (progressDialog != null) progressDialog.dismiss();
        locations = latLngs != null ? latLngs : java.util.Collections.emptyList();
        showMarkers(amount);
    }

    private void showMarkers(int amount) {
        if (nextbillionMap == null || locations == null || mapView.isDestroyed()) {
            return;
        }

        nextbillionMap.clear();
        markerOptionsList.clear();
        if (locations.size() < amount) {
            amount = locations.size();
        }

        showGlMarkers(amount);
    }

    private void showGlMarkers(int amount) {
        DecimalFormat formatter = new DecimalFormat("#.#####");
        Random random = new Random();
        int randomIndex;
        List<LatLng> displayed = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            randomIndex = random.nextInt(locations.size());
            LatLng latLng = locations.get(randomIndex);
            displayed.add(latLng);
            markerOptionsList.add(new MarkerOptions()
                    .position(latLng)
                    .title(String.valueOf(i))
                    .snippet(formatter.format(latLng.getLatitude()) + ", " + formatter.format(latLng.getLongitude())));
        }

        nextbillionMap.addMarkers(markerOptionsList);
        if (displayed.size() == 1) {
            nextbillionMap.moveCamera(ai.nextbillion.maps.camera.CameraUpdateFactory.newLatLngZoom(displayed.get(0), 14));
        } else if (displayed.size() > 1) {
            ai.nextbillion.maps.geometry.LatLngBounds bounds = new ai.nextbillion.maps.geometry.LatLngBounds.Builder()
                    .includes(displayed).build();
            nextbillionMap.moveCamera(ai.nextbillion.maps.camera.CameraUpdateFactory.newLatLngBounds(bounds, 60));
        }
    }

    private void resetMap() {
        if (nextbillionMap == null) {
            return;
        }
        markerOptionsList.clear();
        nextbillionMap.removeAnnotations();
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
        destroyed = true;
        if (loadLocationTask != null) loadLocationTask.cancel(true);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = null;
        mapView.onDestroy();
        nextbillionMap = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private static class LoadLocationTask extends AsyncTask<Void, Integer, List<LatLng>> {

        private WeakReference<MarkersActivity> activity;
        private int amount;
        private final android.content.Context applicationContext;

        private LoadLocationTask(MarkersActivity activity, int amount) {
            this.amount = amount;
            this.applicationContext = activity.getApplicationContext();
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected List<LatLng> doInBackground(Void... params) {
            try {
                String json = GeoParseUtil.loadStringFromResource(applicationContext, R.raw.test_points_utrecht);
                if (!isCancelled()) return GeoParseUtil.parseGeoJsonCoordinates(json);
            } catch (IOException exception) {
                android.util.Log.e("MarkersActivity", "Unable to load bundled marker sample", exception);
            }
            return java.util.Collections.emptyList();
        }

        @Override
        protected void onPostExecute(List<LatLng> locations) {
            super.onPostExecute(locations);
            MarkersActivity activity = this.activity.get();
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                activity.onLatLngListLoaded(locations, amount);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (nextbillionMap == null) {
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.menuItemReset) {
            resetMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
