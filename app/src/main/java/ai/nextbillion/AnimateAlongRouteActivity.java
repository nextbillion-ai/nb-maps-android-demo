package ai.nextbillion;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.kits.geojson.LineString;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.kits.turf.TurfMisc;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.utils.MapSymbol;
import ai.nextbillion.maps.style.layers.Property;
import ai.nextbillion.utils.SymbolGenerator;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static ai.nextbillion.maps.style.layers.Property.ICON_ANCHOR_BOTTOM;

public class AnimateAlongRouteActivity extends AppCompatActivity implements OnMapReadyCallback, NextbillionMap.OnMapClickListener {
    private static final String VEHICLE_ICON_ID = "Vehicle-symbol-Icon";
    private NextbillionMap nextbillionMap;
    private MapView mapView;
    private RouteLine mRouteLine;

    private boolean destroyed;
    private PulsingSymbol originSymbol;
    private MapSymbol vehicleSymbol;
    private MapSymbol destinationSymbol;
    RouteAnimator routeAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        if (destroyed) return;
        this.nextbillionMap = nextbillionMap;
        nextbillionMap.addOnMapClickListener(AnimateAlongRouteActivity.this);
        nextbillionMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (destroyed) return;
//                showDirections();
                mRouteLine = new RouteLine(style, "directions");
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

//    private void showDirections() {
//        NBMapAPIClient client = new NBMapAPIClient();
//        try {
//            client.enQueueGetDirections(new NBLocation(12.96206481, 77.56687669),
//                    new NBLocation(12.99150562, 77.61940507), new Callback<NBDirectionsResponse>() {
//                        @Override
//                        public void onResponse(@NonNull Call<NBDirectionsResponse> call, @NonNull Response<NBDirectionsResponse> response) {
//                            NBDirectionsResponse resp = response.body();
//                            NBRoute route = resp.routes().get(0);
//                            mRouteLine.drawRouteLine(route.geometry());
//                            addWayPoints(route.geometry());
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<NBDirectionsResponse> call, @NonNull Throwable t) {
//
//                        }
//                    });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void addWayPoints(String geometry) {
        // Stop callbacks for the previous route before replacing its symbols and point list.
        if (routeAnimator != null) routeAnimator.cancel();
        LineString routeGeometry = LineString.fromPolyline(geometry, 5);
        List<Point> routePoints = routeGeometry.coordinates();
        Point origin = routePoints.get(0);
        Point dest = routePoints.get(routePoints.size() - 1);
        Bitmap iconBitmap = generateTextIcon();
        LatLng originLatLng = new LatLng(origin.latitude(), origin.longitude());
        LatLng destLatLng = new LatLng(dest.latitude(), dest.longitude());
        initVehicleSymbol(nextbillionMap, mapView, nextbillionMap.getStyle(), originLatLng);
        addOrigin(originLatLng, iconBitmap);
        addDestination(nextbillionMap.getStyle(), iconBitmap, destLatLng);
        routeAnimator = new RouteAnimator(vehicleSymbol, mRouteLine, routePoints, new RouteAnimator.RouteAnimatorCallback() {
            @Override
            public void onAnimationEnd() {

            }
        });
        routeAnimator.start();

    }

    private void addOrigin(LatLng latLng, Bitmap bitmap) {
        if (originSymbol != null) originSymbol.close();
        originSymbol = new PulsingSymbol("directions-origin");
        originSymbol.init(nextbillionMap.getStyle(), bitmap, "directions-origin-image");
        originSymbol.update(latLng);
    }

    private void addDestination(Style style, Bitmap icon, LatLng latLng) {
        InfoWindowSymbol symbol = new InfoWindowSymbol("directions-dest");
        symbols.add(symbol);
        style.addImage("dest-icon", icon);
        symbol.init(style, "dest-icon");
        symbol.update(latLng);
        View view = LayoutInflater.from(this).inflate(R.layout.custom_info_window, mapView, false);
        symbol.addInfoWindow(style, view);
    }

    private void initVehicleSymbol(NextbillionMap nextbillionMap, MapView mapView, Style style, LatLng origin) {
        style.addImage(VEHICLE_ICON_ID, ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_nb_taxi)).getBitmap());
        if (vehicleSymbol != null) vehicleSymbol.close();
        vehicleSymbol = new MapSymbol(style, "route-vehicle", origin, VEHICLE_ICON_ID,
                0.8f, Property.ICON_ANCHOR_CENTER, null);
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
        // Stop Handler work and symbol animations while the map screen is in the background.
        if (routeAnimator != null) routeAnimator.cancel();
        if (vehicleSymbol != null) vehicleSymbol.cancel();
        if (destinationSymbol != null) destinationSymbol.cancel();
        if (originSymbol != null) originSymbol.stopPulsing();
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
        if (routeAnimator != null) {
            routeAnimator.cancel();
        }
        // Release objects that access style sources before MapView destroys its native resources.
        if (vehicleSymbol != null) vehicleSymbol.close();
        if (destinationSymbol != null) destinationSymbol.close();
        if (originSymbol != null) originSymbol.close();
        if (nextbillionMap != null) nextbillionMap.removeOnMapClickListener(this);
        mapView.onDestroy();
        nextbillionMap = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private Bitmap generateTextIcon() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_marker_layout, mapView, false);
        return SymbolGenerator.generate(view);
    }

    List<InfoWindowSymbol> symbols = new ArrayList<>();

    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {
        PointF clickPoint = nextbillionMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = nextbillionMap.queryRenderedFeatures(clickPoint);

        if (!features.isEmpty()) {
            Feature clicked = features.get(0);
            if (clicked.hasProperty(MapSymbol.ID_PROPERTY)) {
                Toast.makeText(this, "Symbol clicked " + clicked.getStringProperty(MapSymbol.ID_PROPERTY),
                        Toast.LENGTH_SHORT).show();
            }
            String symbolId = clicked.hasProperty(InfoWindowSymbol.SYMBOL_ID)
                    ? clicked.getStringProperty(InfoWindowSymbol.SYMBOL_ID) : null;
            if (!TextUtils.isEmpty(symbolId)) {
                for (InfoWindowSymbol symbol : symbols) {
                    if (symbol.getSymbolId().equals(symbolId)) {
                        symbol.onClick();
                    }
                }
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void addWayPoint(NextbillionMap nextbillionMap, MapView mapView, Style style, LatLng latLng, Bitmap icon, String iconId) {
        style.addImage(iconId, icon);
        if (destinationSymbol != null) destinationSymbol.close();
        destinationSymbol = new MapSymbol(style, "route-waypoint", latLng, iconId,
                0.5f, ICON_ANCHOR_BOTTOM, null);
    }

    private Point findSnappedPoint(Location location, List<Point> coordinates) {
        if (coordinates.size() < 2) {
            return Point.fromLngLat(location.getLongitude(), location.getLatitude());
        }

        Point locationToPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

        // Uses Turf's pointOnLine, which takes a Point and a LineString to calculate the closest
        // Point on the LineString.
        Feature feature = TurfMisc.nearestPointOnLine(locationToPoint, coordinates);
        return ((Point) feature.geometry());
    }

}

