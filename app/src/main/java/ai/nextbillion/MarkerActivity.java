package ai.nextbillion;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ai.nextbillion.maps.annotations.Marker;
import ai.nextbillion.maps.annotations.MarkerOptions;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.maps.location.permissions.PermissionsListener;
import ai.nextbillion.maps.location.permissions.PermissionsManager;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.sources.GeoJsonOptions;
import ai.nextbillion.maps.utils.BitmapUtils;

import java.util.List;
import ai.nextbillion.utils.MapSymbol;
import ai.nextbillion.maps.style.layers.Property;


public class MarkerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NextbillionMap mMap;
    private MapSymbol museumSymbol;
    private boolean destroyed;
    private boolean started;
    private boolean locationUpdatesRequested;
    private LocationEngine locationEngine;
    private final LocationEngineCallback<LocationEngineResult> locationEngineListener =
            new LocationEngineCallback<LocationEngineResult>() {
                @Override public void onSuccess(LocationEngineResult result) { }
                @Override public void onFailure(@NonNull Exception error) { }
            };
    private ImageView ivBack;

    static final long DEFAULT_INTERVAL_MILLIS = 1000L;
    static final long DEFAULT_FASTEST_INTERVAL_MILLIS = 1000L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        mapView = findViewById(R.id.map_view);
        ivBack = findViewById(R.id.iv_back);
        mapView.onCreate(savedInstanceState);
        initPermissionManager();

        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        if (destroyed) return;
        mMap = nextbillionMap;
        mMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (destroyed) return;
                addMarker();
                addSymbol(nextbillionMap, mapView, style);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void addMarker() {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(37.98918461647387, 23.732742078407558)));
        mMap.removeMarker(marker);
        mMap.addMarker(new MarkerOptions().position(new LatLng(37.97179697861096, 23.72578634986554)).title("Acropolis of Athens"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(37.96981055262356, 23.73306962989783)).snippet("Temple of Olympian Zeus"));
    }

    private void addSymbol(NextbillionMap nextbillionMap, MapView mapView, Style style){
        style.addImage("SYMBOL_ICON",
                BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.mipmap.museum_icon)),
                false);
        if (museumSymbol != null) museumSymbol.close();
        museumSymbol = new MapSymbol(style, "museum-symbol",
                new LatLng(37.97644990303513, 23.740318392015276),
                "SYMBOL_ICON", 1f, Property.ICON_ANCHOR_CENTER, "Benaki Museum");
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private PermissionsManager permissionsManager;

    private void initPermissionManager(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            fetchLocation();
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(MarkerActivity.this, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (destroyed) return;
                    if (granted) {
                        fetchLocation();
                        mapView.getMapAsync(MarkerActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private void fetchLocation() {
        if (destroyed || !started || locationUpdatesRequested
                || !PermissionsManager.areLocationPermissionsGranted(this)) return;
        if (locationEngine == null) {
            locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());
        }
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_MILLIS)
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL_MILLIS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).build();
        locationEngine.requestLocationUpdates(request, locationEngineListener, Looper.getMainLooper());
        locationUpdatesRequested = true;
    }

    private void stopLocationUpdates() {
        if (locationEngine != null) locationEngine.removeLocationUpdates(locationEngineListener);
        locationUpdatesRequested = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsManager != null) {
            permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        started = true;
        fetchLocation();
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
        started = false;
        stopLocationUpdates();
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        stopLocationUpdates();
        if (museumSymbol != null) {
            museumSymbol.close();
            museumSymbol = null;
        }
        mapView.onDestroy();
        mMap = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
