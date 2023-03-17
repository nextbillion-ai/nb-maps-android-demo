package ai.nextbillion;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.gestures.MoveGestureDetector;
import ai.nextbillion.gestures.Utils;
import ai.nextbillion.maps.annotations.IconFactory;
import ai.nextbillion.maps.annotations.Marker;
import ai.nextbillion.maps.annotations.MarkerOptions;
import ai.nextbillion.maps.annotations.Polygon;
import ai.nextbillion.maps.annotations.PolygonOptions;
import ai.nextbillion.maps.annotations.Polyline;
import ai.nextbillion.maps.annotations.PolylineOptions;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.geometry.LatLngBounds;
import ai.nextbillion.utils.polygon.Point;
import ai.nextbillion.utils.polygon.PolygonUtils;
import ai.nextbillion.view.ColorSelectorView;
import ai.nextbillion.view.SettingSwitchView;
import ai.nextbillion.view.SliderBarView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PolygonActivity extends AppCompatActivity implements View.OnClickListener {

    private NextbillionMap mMap;
    private MapView mapView;
    private BottomSheetBehavior bottomSheetBehavior;
    private Polygon polygon;
    // for stroke
    private Polyline polyline;

    // polygon
    private List<LatLng> polygonPoints = new ArrayList<>();
    private List<Marker> polygonMarkers = new ArrayList<>();
    private LatLngBounds latLngBounds;
    private Marker clickMarker;

    // menu
    private ColorSelectorView polygonColorSelectorView;
    private ColorSelectorView polygonStrokeColorSelectorView;
    private SliderBarView polygonStrokeWidthSliderBarView;
    private SettingSwitchView pointSettingSwitchView;
    private FloatingActionButton fbInfoFloatingActionButton;
    private TextView fbNoticeButton;

    private String polygonColor = "#1E58A5";
    private String polygonStrokeColor = "#FF0000";
    private float stockWidth = 2;
    private int polygonIndex = 1;
    private boolean showPolygonVertex = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polygon);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this::onMapSync);
        // button
        findViewById(R.id.fb_clean).setOnClickListener(this);
        fbInfoFloatingActionButton = findViewById(R.id.fb_expand);
        fbInfoFloatingActionButton.setOnClickListener(this);
        fbNoticeButton = findViewById(R.id.fb_check);
        fbNoticeButton.setOnClickListener(this);
        // menu
        polygonColorSelectorView = findViewById(R.id.polygon_color);
        polygonStrokeColorSelectorView = findViewById(R.id.polygon_stroke_Color);
        polygonStrokeWidthSliderBarView = findViewById(R.id.lineWidth);
        pointSettingSwitchView = findViewById(R.id.pointEnable);
        findViewById(R.id.iv_back).setOnClickListener(this);
        initData();
        initBottomSheet();
    }

    private void initData() {
        polygonColorSelectorView.setTitle(getResources().getString(R.string.polygonColor));
        polygonColorSelectorView.initColor(polygonColor);
        polygonStrokeColorSelectorView.setTitle(getResources().getString(R.string.polygonStrokeColor));
        polygonStrokeColorSelectorView.initColor(polygonStrokeColor);
        polygonStrokeWidthSliderBarView.setTitle(getResources().getString(R.string.polygonStrokeWidth));
        polygonStrokeWidthSliderBarView.initSeekBar(2, 0, 20, "px", 1);
        pointSettingSwitchView.setTitle(getResources().getString(R.string.polygon_point_show));
        pointSettingSwitchView.defaultValue(true);
        polygonColorSelectorView.setOnColorChangedListener(new ColorSelectorView.OnColorChangedListener() {
            @Override
            public void onColorChanged(@NonNull String color) {
                polygonColor = color;
                if (polygon != null) {
                    polygon.setFillColor(Color.parseColor(color));
                }
            }
        });
        polygonStrokeColorSelectorView.setOnColorChangedListener(new ColorSelectorView.OnColorChangedListener() {
            @Override
            public void onColorChanged(@NonNull String color) {
                polygonStrokeColor = color;
                int colorHex = Color.parseColor(color);
                if (polyline != null) {
                    polyline.setColor(colorHex);
                }
            }
        });
        polygonStrokeWidthSliderBarView.setOnSliderChangedListener(new SliderBarView.OnSliderChangedListener() {
            @Override
            public void onSliderChanged(float value) {
                stockWidth = value;
                if (polyline != null) {
                    polyline.setWidth(value);
                }
            }
        });

        pointSettingSwitchView.setOnSwitchChangedLister(new SettingSwitchView.OnSwitchChangedLister() {
            @Override
            public void onSwitchChanged(boolean status) {
                showPolygonVertex = status;
                upDataMarkerBySwitchState();
            }
        });
    }

    private void upDataMarkerBySwitchState() {
        if (polygonMarkers == null || polygonMarkers.isEmpty()) {
            return;
        }
        for (Marker marker : polygonMarkers) {
            marker.setIcon(IconFactory.getInstance(PolygonActivity.this)
                    .fromResource((showPolygonVertex || polygonPoints.size() < 3) ? R.mipmap.ic_blue_dot : R.mipmap.ic_marker_transparent));
        }
    }

    private void onMapSync(NextbillionMap map) {
        mMap = map;
        addMapListener();
        mMap.setStyle(new Style.Builder().fromUri("https://api.nextbillion.io/maps/openstreetmap/style.json"));
    }

    private void addMapListener() {
        mMap.addOnMapLongClickListener(new NextbillionMap.OnMapLongClickListener() {
            @Override
            public boolean onMapLongClick(@NonNull LatLng latLng) {
                polygonPoints.add(latLng);
                MarkerOptions markerOptions = new MarkerOptions()
                        .setTitle(String.valueOf(polygonIndex))
                        .position(latLng)
                        .setIcon(IconFactory.getInstance(PolygonActivity.this)
                                .fromResource((showPolygonVertex || polygonPoints.size() < 3) ? R.mipmap.ic_blue_dot : R.mipmap.ic_marker_transparent));
                polygonMarkers.add(mMap.addMarker(markerOptions));
                polygonIndex ++;
                if (polygonPoints.size() > 2) {
                    createPolygon();
                }
                return false;
            }
        });
        mMap.addOnMapClickListener(new NextbillionMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng latLng) {
                if (clickMarker != null) {
                    mMap.removeMarker(clickMarker);
                }
                clickMarker = mMap.addMarker(latLng);
                Point clickPoint = new Point(latLng.getLongitude(),latLng.getLatitude());
                List<Point> polygonLngLatPoints = new ArrayList<>();
                for (LatLng point : polygonPoints) {
                    polygonLngLatPoints.add(new Point(point.getLongitude(),point.getLatitude()));
                }
                boolean isPointInPolygon = PolygonUtils.isPointInPolygon(clickPoint,PolygonUtils.getPolygon(true,polygonLngLatPoints));
                if (isPointInPolygon) {
                    Toast.makeText(PolygonActivity.this, "this point is in polygon", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        mMap.addOnMoveListener(new NextbillionMap.OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {

            }

            @Override
            public void onMove(@NonNull MoveGestureDetector moveGestureDetector) {

            }

            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
                boolean isPolygonInScreen = checkPolygonVisibility();
                if (isPolygonInScreen) {
                    fbNoticeButton.setBackgroundResource(R.drawable.shape_circle_40_purple_200);
                } else {
                    fbNoticeButton.setBackgroundResource(R.drawable.shape_circle_40_md_grey_400);
                }
            }
        });
        mMap.setOnMarkerClickListener(new NextbillionMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker == clickMarker) {
                    mMap.removeMarker(clickMarker);
                    clickMarker = null;
                }
                return false;
            }
        });
    }

    private boolean checkPolygonVisibility() {
        if (mMap == null || mMap.getProjection() == null || latLngBounds == null) {
            return false;
        }
        LatLngBounds isInScreen = mMap.getProjection().getVisibleRegion().latLngBounds.intersect(latLngBounds);
        return isInScreen != null;
    }

    private void cleanPolygon() {
        if (mMap == null || polygon == null) {
            Toast.makeText(PolygonActivity.this, "No polygon need remove", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.removePolygon(polygon);
        polygon = null;
        polygonPoints.clear();
        latLngBounds = null;
        // polygon marker
        for (Marker marker : polygonMarkers) {
            mMap.removeMarker(marker);
        }
        polygonMarkers.clear();

        // line
        if (polyline != null) {
            mMap.removePolyline(polyline);
            polyline = null;
        }
    }

    private void createPolygon() {
        // check map
        if (mMap == null) {
            Toast.makeText(this, getResources().getString(R.string.waite_notice), Toast.LENGTH_SHORT).show();
            return;
        }
        // check data
        if (polygonPoints.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.polygon_empty_point_notice), Toast.LENGTH_SHORT).show();
            return;
        }

        if (polyline != null) {
            mMap.removePolyline(polyline);
        }

        if (polygon != null) {
            mMap.removePolygon(polygon);
        }
        // update marker show style
        upDataMarkerBySwitchState();

        // add polygon
        latLngBounds = new LatLngBounds.Builder().includes(polygonPoints).build();
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(polygonPoints)
                .fillColor(Color.parseColor(polygonColor))
                .strokeColor(Color.parseColor(polygonStrokeColor));
        polygon = mMap.addPolygon(polygonOptions);

        // add polygon stroke by polyline
        List<LatLng> polylinePoints = new ArrayList<>(polygonPoints);
        polylinePoints.add(polygonPoints.get(0));
        polylinePoints.add(polygonPoints.get(1));
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(polylinePoints)
                .color(Color.parseColor(polygonStrokeColor))
                .width(stockWidth);
        polyline = mMap.addPolyline(polylineOptions);
        // change check button color
        fbNoticeButton.setBackgroundResource(R.drawable.shape_circle_40_purple_200);
    }

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setPeekHeight((int) Utils.dpToPx(60));
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    fbInfoFloatingActionButton.setVisibility(View.VISIBLE);
                } else {
                    fbInfoFloatingActionButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fb_clean) {
            cleanPolygon();
        } else if (v.getId() == R.id.fb_expand) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (v.getId() == R.id.fb_check) {
            boolean isPolygonInScreen = checkPolygonVisibility();
            String message = getResources().getString(R.string.polygin_visibility_notice);
            if (!isPolygonInScreen) {
                message = getResources().getString(R.string.polygon_invisibility_notice);
            }
            Toast.makeText(PolygonActivity.this, message, Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.iv_back) {
            finish();
        }
    }
}
