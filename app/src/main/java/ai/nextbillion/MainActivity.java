package ai.nextbillion;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.camera.CameraAnimationDelegate;
import ai.nextbillion.dialog.InputDialog;
import ai.nextbillion.dialog.LogoPickerDialog;
import ai.nextbillion.geocoding.model.LocationSuggestion;
import ai.nextbillion.geocoding.model.SearchItem;
import ai.nextbillion.geocoding.rest.SearchResponse;
import ai.nextbillion.gestures.MoveGestureDetector;
import ai.nextbillion.maps.annotations.Marker;
import ai.nextbillion.maps.annotations.MarkerOptions;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.location.LocationComponent;
import ai.nextbillion.maps.location.LocationComponentActivationOptions;
import ai.nextbillion.maps.location.LocationComponentOptions;
import ai.nextbillion.maps.location.OnCameraTrackingChangedListener;
import ai.nextbillion.maps.location.engine.LocationEngine;
import ai.nextbillion.maps.location.engine.LocationEngineCallback;
import ai.nextbillion.maps.location.engine.LocationEngineProvider;
import ai.nextbillion.maps.location.engine.LocationEngineRequest;
import ai.nextbillion.maps.location.engine.LocationEngineResult;
import ai.nextbillion.maps.location.modes.CameraMode;
import ai.nextbillion.maps.location.modes.RenderMode;
import ai.nextbillion.maps.location.permissions.PermissionsListener;
import ai.nextbillion.maps.location.permissions.PermissionsManager;
import ai.nextbillion.service.GeoCodingRequest;
import ai.nextbillion.utils.CameraAnimateUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NextbillionMap.OnMapClickListener, LocationEngineCallback<LocationEngineResult>, NextbillionMap.OnMoveListener, OnCameraTrackingChangedListener {

    private MapView mapView;
    private LogoPickerDialog logoPickerDialog;
    private ImageView backBtn;

    private CameraAnimationDelegate delegate;
    NextbillionMap nextbillionMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private List<Marker> mapMarkers = new ArrayList();
    private boolean touchEnable = true;
    private LocationEngine locationEngine;
    private ImageView mActionButton;
    private BottomSheetBehavior logoViewBehavior;
    private Location currentLocation;
    private FloatingSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationEngine = LocationEngineProvider.getBestLocationEngine(this, false);
        searchView = findViewById(R.id.floating_search_view);
        mActionButton = findViewById(R.id.trackLocation);
        mapView = findViewById(R.id.map_view);
        logoPickerDialog = findViewById(R.id.logo_view);

        backBtn = findViewById(R.id.back_ib);

        mapView.onCreate(savedInstanceState);

        logoViewBehavior = BottomSheetBehavior.from(logoPickerDialog);
        logoViewBehavior.setHideable(true);
        logoViewBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        checkPermissions();
        setUpSuggestion();

        setListeners();
    }

    private void setUpSuggestion() {

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                LocationSuggestion locationSuggestion = (LocationSuggestion) searchSuggestion;
                onSearchItemClicked(locationSuggestion);
            }

            @Override
            public void onSearchAction(String currentQuery) {
                searchView.hideProgress();
                searchLocation(currentQuery, true);
            }
        });


        searchView.setOnQueryChangeListener((oldQuery, newQuery) -> {
            if (!oldQuery.equals("") && newQuery.equals("")) {
                searchView.clearSuggestions();
            } else {
                if (!TextUtils.isEmpty(newQuery)) {
                    searchView.showProgress();
                    searchLocation(newQuery, false);
                }
            }
        });

        searchView.setOnBindSuggestionCallback((suggestionView, leftIcon, textView, item, itemPosition) -> {
            leftIcon.setVisibility(View.GONE);
            String textLight = "#787878";
            textView.setTextColor(Color.BLACK);
            String text = item.getBody()
                    .replaceFirst(searchView.getQuery(),
                            "<font color=\"" + textLight + "\">" + searchView.getQuery() + "</font>");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(text));
            }

        });

        searchView.setOnMenuItemClickListener(this::onOptionsItemSelected);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logoViewBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        switch (item.getItemId()) {
            case R.id.action_o1_d1:
                moveLatLng();
                return true;
            case R.id.action_clear_marker:
                clearAllMarker();
                return true;
            case R.id.action_o2_d1:
                logoViewBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                mActionButton.setVisibility(View.GONE);
                return true;
            case R.id.action_o2_d2:
                touchEnable = !touchEnable;
                String title = touchEnable ? "Disable touch event" : "Enable touch event";
                item.setTitle(title);
                setupTouchEvent(touchEnable);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSearchItemClicked(LocationSuggestion locationSuggestion) {
        searchView.clearSuggestions();
        searchView.clearSearchFocus();
        searchView.clearQuery();
        LatLng latLng = new LatLng(locationSuggestion.lat, locationSuggestion.lng);
        flyTo(latLng, locationSuggestion.subTitle);
    }

    private void searchLocation(String currentQuery, boolean searchAction) {
        if (currentLocation != null) {
            retrofit2.Callback<SearchResponse> callback = new retrofit2.Callback<SearchResponse>() {
                @Override
                public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                    if (response.body() == null) {
                        onSearchFailed();
                        return;
                    }
                    if (response.code() < 200 || response.code() >= 300) {
                        onSearchFailed();
                        return;
                    }
                    // Response success
                    SearchResponse searchResponse = response.body();
                    if (searchResponse.items != null && !searchResponse.items.isEmpty()) {
                        searchView.hideProgress();
                        searchView.swapSuggestions(convertSearchResult(searchResponse.items));

                        if (searchAction) {
                            onSearchItemClicked(convertSearchResult(searchResponse.items).get(0));
                        }
                        return;
                    }
                    onSearchFailed();
                }

                @Override
                public void onFailure(Call<SearchResponse> call, Throwable t) {
                    onSearchFailed();
                }
            };

            GeoCodingRequest.searchLocation(currentQuery, currentLocation, callback);
        }
    }

    private void onSearchFailed() {
        searchView.hideProgress();
        searchView.clearSuggestions();
        Toast.makeText(this, getResources().getString(R.string.search_empty_notice), Toast.LENGTH_SHORT).show();
    }


    private List<LocationSuggestion> convertSearchResult(List<SearchItem> items) {
        List<LocationSuggestion> locationSuggestions = new ArrayList<>();
        for (SearchItem item : items) {
            locationSuggestions.add(new LocationSuggestion(item));
        }
        return locationSuggestions;
    }

    @SuppressLint("MissingPermission")
    private void setListeners() {
        mActionButton.setOnClickListener(v -> {
            trackMyCurrentLocation();
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupLogoActionBottomSheet();
    }

    private void trackMyCurrentLocation() {
        if (locationComponent == null) {
            return;
        }
        mActionButton.setImageResource(R.drawable.ic_my_location);
        locationComponent.setCameraMode(CameraMode.TRACKING);
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nbMap) {
        this.nextbillionMap = nbMap;
        nextbillionMap.getStyle(this::buildEngineRequest);
        nextbillionMap.setMinZoomPreference(5);
        delegate = new CameraAnimationDelegate(nextbillionMap);
        nextbillionMap.getUiSettings().setAttributionEnabled(false);
        nextbillionMap.addOnMapClickListener(this);
        nextbillionMap.addOnMoveListener(this);
        logoPickerDialog.setupSwitchButton(nextbillionMap.getUiSettings().isLogoEnabled());
    }

    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {
        flyTo(latLng);
        return false;
    }

    private void flyTo(LatLng latLng) {
        flyTo(latLng, "");

    }

    private void flyTo(LatLng latLng, String locationName) {
        onMapMoveBegin();
        String latLngInfo = new DecimalFormat("#.#####").format(latLng.getLatitude()) + ", "
                + new DecimalFormat("#.#####").format(latLng.getLongitude());
        Marker marker = nextbillionMap.addMarker(new MarkerOptions().position(latLng).title(latLngInfo).snippet(locationName));
        mapMarkers.add(marker);
        CameraAnimateUtils.animateCamera(nextbillionMap, latLng);

    }

    private void resetMapLogo(int resource) {
        nextbillionMap.getUiSettings().setLogoResourceId(resource);
    }

    private void updateLogoEnable(boolean enable) {
        nextbillionMap.getUiSettings().setLogoEnabled(enable);
    }

    private void setupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        popupMenu.getMenuInflater().inflate(R.menu.simple_map_menu_matrix, popupMenu.getMenu());

        MenuItem item = popupMenu.getMenu().getItem(2);
        String title = touchEnable ? "Disable touch event" : "Enable touch event";
        item.setTitle(title);

        popupMenu.setOnMenuItemClickListener(item1 -> {
            switch (item1.getItemId()) {
                case R.id.action_o1_d1:
                    moveLatLng();
                    return true;
                case R.id.action_o1_d2:
                    clearAllMarker();
                    return true;
                case R.id.action_o2_d1:
                    logoViewBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    mActionButton.setVisibility(View.GONE);
                    return true;
                case R.id.action_o2_d2:
                    touchEnable = !touchEnable;
                    String title1 = touchEnable ? "Disable touch event" : "Enable touch event";
                    item1.setTitle(title1);
                    setupTouchEvent(touchEnable);
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void moveLatLng() {
        InputDialog dialog = new InputDialog(new InputDialog.InputTextCallback() {
            @Override
            public void onInput(String lat, String lot) {
                boolean invalid = lat == null || lat.isEmpty() || lot == null || lot.isEmpty();
                if (invalid) {
                    Toast.makeText(MainActivity.this, "Input is invalid", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    double latDouble = Double.parseDouble(lat);
                    double lotDouble = Double.parseDouble(lot);
                    LatLng latLng = new LatLng(latDouble, lotDouble);
                    flyTo(latLng);
                } catch (Exception e) {
                    invalid = true;
                }
                if (invalid) {
                    Toast.makeText(MainActivity.this, "Input is invalid", Toast.LENGTH_LONG).show();
                }

            }
        });
        dialog.showDialog(this);
    }

    private void clearAllMarker() {
        for (Marker marker : mapMarkers){
            nextbillionMap.removeMarker(marker);
        }
        mapMarkers.clear();
//        nextbillionMap.clear();
        trackMyCurrentLocation();
    }

    private void setupTouchEvent(boolean enable) {
        nextbillionMap.getUiSettings().setAllGesturesEnabled(enable);
    }

    private void setupLogoActionBottomSheet() {
        logoPickerDialog.setupListener(new LogoPickerDialog.LogoOnClickListener() {
            @Override
            public void onSelect(int resource, boolean checked) {
                logoViewBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mActionButton.setVisibility(View.VISIBLE);
                if (resource != 0) {
                    resetMapLogo(resource);
                }

                if (checked != nextbillionMap.getUiSettings().isLogoEnabled()) {
                    updateLogoEnable(checked);
                }
            }

            @Override
            public void onCancel() {
                logoViewBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mActionButton.setVisibility(View.VISIBLE);
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
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(this);
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // Location
    @SuppressLint("MissingPermission")
    private void buildEngineRequest(Style style) {
        locationComponent = nextbillionMap.getLocationComponent();

        LocationEngineRequest request = new LocationEngineRequest.Builder(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(500)
                .build();

        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions
                        .builder(this, style)
                        .locationComponentOptions(
                                LocationComponentOptions.builder(this).pulseEnabled(true)
                                        .build())
                        .locationEngine(locationEngine)
                        .locationEngineRequest(request)
                        .build());

        // Enable Tracking Location Component
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setRenderMode(RenderMode.NORMAL);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.addOnCameraTrackingChangedListener(this);
        mActionButton.setImageResource(R.drawable.ic_my_location);

        locationEngine.requestLocationUpdates(request, this, null);
        locationEngine.getLastLocation(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkPermissions() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapView.getMapAsync(this);
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(MainActivity.this, "You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        mapView.getMapAsync(MainActivity.this);
                    } else {
                        finish();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onSuccess(LocationEngineResult locationEngineResult) {
        if (locationEngineResult == null || locationEngineResult.getLastLocation() == null) {
            return;
        }
        currentLocation = locationEngineResult.getLastLocation();
    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
        onMapMoveBegin();
    }

    private void onMapMoveBegin() {
    }

    @Override
    public void onMove(@NonNull MoveGestureDetector moveGestureDetector) {
    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

    }

    @Override
    public void onCameraTrackingDismissed() {
        mActionButton.setImageResource(R.drawable.icon_location_searching);
    }

    @Override
    public void onCameraTrackingChanged(int i) {

    }
}