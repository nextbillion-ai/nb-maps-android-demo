package ai.nextbillion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.LocationComponent;
import ai.nextbillion.maps.location.LocationComponentActivationOptions;
import ai.nextbillion.maps.location.LocationComponentOptions;
import ai.nextbillion.maps.location.modes.CameraMode;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.maps.location.permissions.PermissionsListener;
import ai.nextbillion.maps.location.permissions.PermissionsManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CustomMapStyleActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NextbillionMap nextbillionMap;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_layer_map_change);

        mapView = findViewById(R.id.mapView);
        FloatingActionButton stylesFab = findViewById(R.id.fabStyles);

        stylesFab.setOnClickListener(v -> {
            if (nextbillionMap != null) {
                toggleMapStyle();
            }
        });

        mapView.onCreate(savedInstanceState);

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(CustomMapStyleActivity.this, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(CustomMapStyleActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void toggleMapStyle() {
        nextbillionMap.getStyle(style -> {
            String styleUrl = StyleConstants.DARK.equals(style.getUri()) ? StyleConstants.LIGHT : StyleConstants.DARK;
            nextbillionMap.setStyle(new Style.Builder().fromUri(styleUrl));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        this.nextbillionMap = nextbillionMap;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(22.70418008712976, 78.66264025041812))
                .zoom(6)
                .tilt(30)
                .tilt(0)
                .build();
        nextbillionMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        nextbillionMap.setStyle(new Style.Builder().fromUri(StyleConstants.LIGHT),
                style -> activateLocationComponent(style));
    }

    @SuppressLint("MissingPermission")
    private void activateLocationComponent(@NonNull Style style) {
        LocationComponent locationComponent = nextbillionMap.getLocationComponent();

        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions
                        .builder(this, style)
                        .useDefaultLocationEngine(true)
                        .locationComponentOptions(LocationComponentOptions.builder(this)
                                .pulseEnabled(true)
                                .build())
                        .build());

        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.COMPASS);
        locationComponent.setCameraMode(CameraMode.TRACKING);

        locationComponent.addOnLocationClickListener(
                () -> Toast.makeText(this, "Location clicked", Toast.LENGTH_SHORT).show());

        locationComponent.addOnLocationLongClickListener(
                () -> Toast.makeText(this, "Location long clicked", Toast.LENGTH_SHORT).show());
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
