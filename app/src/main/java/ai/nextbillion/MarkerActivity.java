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

import ai.nextbillion.maps.plugins.annotation.Symbol;
import ai.nextbillion.maps.plugins.annotation.SymbolManager;
import ai.nextbillion.maps.plugins.annotation.SymbolOptions;

public class MarkerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NextbillionMap mMap;
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
        mMap = nextbillionMap;
        mMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addMarker();
                addSymbol(nextbillionMap, mapView, style);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void addMarker() {
        Marker marker = mMap.addMarker(new LatLng(37.98918461647387, 23.732742078407558));
        mMap.removeMarker(marker);
        mMap.addMarker(new MarkerOptions().position(new LatLng(37.97179697861096, 23.72578634986554)).title("Acropolis of Athens"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(37.96981055262356, 23.73306962989783)).snippet("Temple of Olympian Zeus"));
    }

    private void addSymbol(NextbillionMap nextbillionMap, MapView mapView, Style style){
        style.addImage("SYMBOL_ICON",
                BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.mipmap.museum_icon)),
                false);
        GeoJsonOptions options = new GeoJsonOptions().withTolerance(0.4f);
        SymbolManager symbolManager = new SymbolManager(mapView, nextbillionMap, style, null, options);
        SymbolOptions symbolOptions = new SymbolOptions()
                .withLatLng(new LatLng(37.97644990303513, 23.740318392015276))
                .withIconImage("SYMBOL_ICON")
                .withTextField("Benaki Museum");
        Symbol symbol = symbolManager.create(symbolOptions);
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

    private void fetchLocation(){
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest locationEngineRequest =
                new LocationEngineRequest.Builder(DEFAULT_INTERVAL_MILLIS)
                        .setFastestInterval(DEFAULT_FASTEST_INTERVAL_MILLIS)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .build();

         LocationEngineCallback<LocationEngineResult> locationEngineListener
                = new LocationEngineCallback<LocationEngineResult>() {
             @Override
             public void onSuccess(LocationEngineResult result) {

             }

             @Override
             public void onFailure(@NonNull Exception e) {

             }
         };
         locationEngine.requestLocationUpdates(locationEngineRequest, locationEngineListener, Looper.getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}
