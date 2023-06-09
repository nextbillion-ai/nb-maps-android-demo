package ai.nextbillion;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.location.LocationComponent;
import ai.nextbillion.maps.location.LocationComponentActivationOptions;
import ai.nextbillion.maps.location.LocationComponentOptions;
import ai.nextbillion.maps.location.OnCameraTrackingChangedListener;
import ai.nextbillion.maps.location.OnLocationCameraTransitionListener;
import ai.nextbillion.maps.location.OnLocationClickListener;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.modes.CameraMode;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.maps.location.permissions.PermissionsListener;
import ai.nextbillion.maps.location.permissions.PermissionsManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;

public class LocationModesActivity extends AppCompatActivity implements OnMapReadyCallback,
        OnLocationClickListener, OnCameraTrackingChangedListener {

    private MapView mapView;
    private Button locationModeBtn;
    private Button locationTrackingBtn;
    private View protectedGestureArea;

    private PermissionsManager permissionsManager;

    private LocationComponent locationComponent;
    private NextbillionMap nextbillionMap;
    private boolean defaultStyle = false;

    private static final String SAVED_STATE_CAMERA = "saved_state_camera";
    private static final String SAVED_STATE_RENDER = "saved_state_render";
    private static final String SAVED_STATE_LOCATION = "saved_state_location";

    @CameraMode.Mode
    private int cameraMode = CameraMode.TRACKING;

    @RenderMode.Mode
    private int renderMode = RenderMode.NORMAL;

    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_modes);

        mapView = findViewById(R.id.mapView);
        protectedGestureArea = findViewById(R.id.view_protected_gesture_area);

        locationModeBtn = findViewById(R.id.button_location_mode);
        locationModeBtn.setOnClickListener(v -> {
            if (locationComponent == null) {
                return;
            }
            showModeListDialog();
        });

        locationTrackingBtn = findViewById(R.id.button_location_tracking);
        locationTrackingBtn.setOnClickListener(v -> {
            if (locationComponent == null) {
                return;
            }
            showTrackingListDialog();
        });


        if (savedInstanceState != null) {
            cameraMode = savedInstanceState.getInt(SAVED_STATE_CAMERA);
            renderMode = savedInstanceState.getInt(SAVED_STATE_RENDER);
            lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
        }

        mapView.onCreate(savedInstanceState);

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(LocationModesActivity.this, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(LocationModesActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        this.nextbillionMap = nextbillionMap;
        nextbillionMap.animateCamera(CameraUpdateFactory.zoomBy(13));
        nextbillionMap.setStyle(StyleConstants.NBMAP_STREETS, style -> {
            locationComponent = nextbillionMap.getLocationComponent();
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions
                            .builder(this, style)
                            .useSpecializedLocationLayer(true)
                            .useDefaultLocationEngine(true)
                            .locationEngineRequest(new LocationEngineRequest.Builder(750)
                                    .setFastestInterval(750)
                                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                                    .build())
                            .build());

            locationComponent.setLocationComponentEnabled(true);
            locationComponent.addOnLocationClickListener(this);
            locationComponent.addOnCameraTrackingChangedListener(this);
            locationComponent.setCameraMode(cameraMode);
            setRendererMode(renderMode);
            locationComponent.forceLocationUpdate(lastLocation);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_mode, menu);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (locationComponent == null) {
            return super.onOptionsItemSelected(item);
        }

        int id = item.getItemId();
        if (id == R.id.action_component_disable) {
            locationComponent.setLocationComponentEnabled(false);
            return true;
        } else if (id == R.id.action_component_enabled) {
            locationComponent.setLocationComponentEnabled(true);
            return true;
        } else if (id == R.id.action_gestures_management_disabled) {
            disableGesturesManagement();
            return true;
        } else if (id == R.id.action_gestures_management_enabled) {
            enableGesturesManagement();
            return true;
        } else if (id == R.id.action_component_throttling_enabled) {
            locationComponent.setMaxAnimationFps(5);
        } else if (id == R.id.action_component_throttling_disabled) {
            locationComponent.setMaxAnimationFps(Integer.MAX_VALUE);
        } else if (id == R.id.action_component_animate_while_tracking) {
            locationComponent.zoomWhileTracking(17, 750, new NextbillionMap.CancelableCallback() {
                @Override
                public void onCancel() {
                    // No impl
                }

                @Override
                public void onFinish() {
                    locationComponent.tiltWhileTracking(60);
                }
            });
            if (locationComponent.getCameraMode() == CameraMode.NONE) {

                Toast.makeText(this, "Not possible to animate - not tracking", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void disableGesturesManagement() {
        if (locationComponent == null) {
            return;
        }

        protectedGestureArea.getLayoutParams().height = 0;
        protectedGestureArea.getLayoutParams().width = 0;

        LocationComponentOptions options = locationComponent
                .getLocationComponentOptions()
                .toBuilder()
                .trackingGesturesManagement(false)
                .build();
        locationComponent.applyStyle(options);
    }

    private void enableGesturesManagement() {
        if (locationComponent == null) {
            return;
        }

        RectF rectF = new RectF(0f, 0f, mapView.getWidth() / 2f, mapView.getHeight() / 2f);
        protectedGestureArea.getLayoutParams().height = (int) rectF.bottom;
        protectedGestureArea.getLayoutParams().width = (int) rectF.right;

        LocationComponentOptions options = locationComponent
                .getLocationComponentOptions()
                .toBuilder()
                .trackingGesturesManagement(true)
                .trackingMultiFingerProtectedMoveArea(rectF)
                .trackingMultiFingerMoveThreshold(500)
                .build();
        locationComponent.applyStyle(options);
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

    @SuppressLint("MissingPermission")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putInt(SAVED_STATE_CAMERA, cameraMode);
        outState.putInt(SAVED_STATE_RENDER, renderMode);
        if (locationComponent != null) {
            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
        }
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

    @Override
    public void onLocationComponentClick() {
        Toast.makeText(this, "OnLocationComponentClick", Toast.LENGTH_LONG).show();
    }

    private void showModeListDialog() {
        List<String> modes = new ArrayList<>();
        modes.add("Normal");
        modes.add("Compass");
        modes.add("GPS");
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, modes);
        ListPopupWindow listPopup = new ListPopupWindow(this);
        listPopup.setAdapter(profileAdapter);
        listPopup.setAnchorView(locationModeBtn);
        listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
            String selectedMode = modes.get(position);
            locationModeBtn.setText(selectedMode);
            if (selectedMode.contentEquals("Normal")) {
                setRendererMode(RenderMode.NORMAL);
            } else if (selectedMode.contentEquals("Compass")) {
                setRendererMode(RenderMode.COMPASS);
            } else if (selectedMode.contentEquals("GPS")) {
                setRendererMode(RenderMode.GPS);
            }
            listPopup.dismiss();
        });
        listPopup.show();
    }

    private void setRendererMode(@RenderMode.Mode int mode) {
        renderMode = mode;
        locationComponent.setRenderMode(mode);
        if (mode == RenderMode.NORMAL) {
            locationModeBtn.setText("Normal");
        } else if (mode == RenderMode.COMPASS) {
            locationModeBtn.setText("Compass");
        } else if (mode == RenderMode.GPS) {
            locationModeBtn.setText("Gps");
        }
    }

    private void showTrackingListDialog() {
        List<String> trackingTypes = new ArrayList<>();
        trackingTypes.add("None");
        trackingTypes.add("None Compass");
        trackingTypes.add("None GPS");
        trackingTypes.add("Tracking");
        trackingTypes.add("Tracking Compass");
        trackingTypes.add("Tracking GPS");
        trackingTypes.add("Tracking GPS North");
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, trackingTypes);
        ListPopupWindow listPopup = new ListPopupWindow(this);
        listPopup.setAdapter(profileAdapter);
        listPopup.setAnchorView(locationTrackingBtn);
        listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
            String selectedTrackingType = trackingTypes.get(position);
            locationTrackingBtn.setText(selectedTrackingType);
            if (selectedTrackingType.contentEquals("None")) {
                setCameraTrackingMode(CameraMode.NONE);
            } else if (selectedTrackingType.contentEquals("None Compass")) {
                setCameraTrackingMode(CameraMode.NONE_COMPASS);
            } else if (selectedTrackingType.contentEquals("None GPS")) {
                setCameraTrackingMode(CameraMode.NONE_GPS);
            } else if (selectedTrackingType.contentEquals("Tracking")) {
                setCameraTrackingMode(CameraMode.TRACKING);
            } else if (selectedTrackingType.contentEquals("Tracking Compass")) {
                setCameraTrackingMode(CameraMode.TRACKING_COMPASS);
            } else if (selectedTrackingType.contentEquals("Tracking GPS")) {
                setCameraTrackingMode(CameraMode.TRACKING_GPS);
            } else if (selectedTrackingType.contentEquals("Tracking GPS North")) {
                setCameraTrackingMode(CameraMode.TRACKING_GPS_NORTH);
            }
            listPopup.dismiss();
        });
        listPopup.show();
    }

    private void setCameraTrackingMode(@CameraMode.Mode int mode) {
        locationComponent.setCameraMode(mode, 1200, 16.0, null, 45.0,
                new OnLocationCameraTransitionListener() {
                    @Override
                    public void onLocationCameraTransitionFinished(@CameraMode.Mode int cameraMode) {
                        Toast.makeText(LocationModesActivity.this, "Transition finished", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLocationCameraTransitionCanceled(@CameraMode.Mode int cameraMode) {
                        Toast.makeText(LocationModesActivity.this, "Transition canceled", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCameraTrackingDismissed() {
        locationTrackingBtn.setText("None");
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {
        this.cameraMode = currentMode;
        if (currentMode == CameraMode.NONE) {
            locationTrackingBtn.setText("None");
        } else if (currentMode == CameraMode.NONE_COMPASS) {
            locationTrackingBtn.setText("None Compass");
        } else if (currentMode == CameraMode.NONE_GPS) {
            locationTrackingBtn.setText("None GPS");
        } else if (currentMode == CameraMode.TRACKING) {
            locationTrackingBtn.setText("Tracking");
        } else if (currentMode == CameraMode.TRACKING_COMPASS) {
            locationTrackingBtn.setText("Tracking Compass");
        } else if (currentMode == CameraMode.TRACKING_GPS) {
            locationTrackingBtn.setText("Tracking GPS");
        } else if (currentMode == CameraMode.TRACKING_GPS_NORTH) {
            locationTrackingBtn.setText("Tracking GPS North");
        }
    }
}
