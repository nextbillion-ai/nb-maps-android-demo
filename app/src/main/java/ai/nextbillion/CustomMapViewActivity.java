package ai.nextbillion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;

public class CustomMapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NextbillionMap mMap;

    private final LatLng cameraTargetPosition = new LatLng(53.550813508267716, 9.992248999933745);
    private final int CAMERA_ZOOM_LEVEL = 14;
    private final int CAMERA_TILT = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_map_view);
        mapView = findViewById(R.id.custom_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void updateMapUISetting() {
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setCompassImage(getResources().getDrawable(R.drawable.custom_user_puck_icon));
        mMap.getUiSettings().setLogoEnabled(true);
        mMap.getUiSettings().setLogoResourceId(R.mipmap.map_log_1);
        mMap.getUiSettings().setAttributionEnabled(true);
        mMap.getUiSettings().setAttributionTintColor(getResources().getColor(R.color.palette_mint_100));
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        mMap = nextbillionMap;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(cameraTargetPosition)
                .zoom(CAMERA_ZOOM_LEVEL)
                .tilt(CAMERA_TILT)
                .build();
        mMap.setCameraPosition(cameraPosition);

        updateMapUISetting();

        mMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
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
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
