package ai.nextbillion;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import ai.nextbillion.api.models.NBLocation;
import ai.nextbillion.api.models.directions.NBRoute;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.nbaiapiclient.NBMapAPIClient;
import ai.nextbillion.nbaiapiclient.NBRouteLine;
import ai.nextbillion.nbaiapiclient.NBRouteLineCallback;
import ai.nextbillion.nbaiapiclient.NBRouteLineConfig;

/**
 * To use this feature in your own project, you can simple copy all files under the folder of nbaiapiclient.
 * Before using APIClient, please make sure you have added dependencies:
 *     implementation 'ai.nextbillion:nb-maps-android:0.1.0'
 *     implementation 'ai.nextbillion:nb-kits-api:0.0.2'
 */
public class APIClientDemoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NextbillionMap nextbillionMap;
    private NBMapAPIClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_client);
        mapView = findViewById(R.id.mapview);
        mapView.getMapAsync(this);
        initAPIClient();
        findViewById(R.id.back_ib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initAPIClient() {
        client = new NBMapAPIClient();
    }

    @Override
    public void onMapReady(@NonNull NextbillionMap nextbillionMap) {
        this.nextbillionMap = nextbillionMap;
        moveToSingapore();
        nextbillionMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                fetchRouteAndRenderOnMaps();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private void moveToSingapore() {
        LatLng singapore = new LatLng(1.30683491981202, 103.81821918219501);
        int millisecondSpeed = 300;
        CameraPosition position = new CameraPosition.Builder()
                .target(singapore)
                .zoom(12)
                .build();
        nextbillionMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), millisecondSpeed);
    }

    private void fetchRouteAndRenderOnMaps() {
        NBLocation origin = new NBLocation(1.2943697224834985, 103.83174518020617);
        NBLocation destination = new NBLocation(1.3303739400394545, 103.80463752235354);
        NBRouteLineConfig.Builder configBuilder = new NBRouteLineConfig.Builder();
        //other than specific a route name, NBRouteConfig is also able to configure many route line properties such as:
        // lineWidth, lineCap, lineJoin, lineColor, the drawable resource of origin or destination icon.
        configBuilder.setRouteName("Test_route_line_displaying");

        client.showDirections(origin, destination, configBuilder.build(), nextbillionMap, this, new NBRouteLineCallback() {
            @Override
            public void onRouteRendered(NBRoute route, NBRouteLine routeLine) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }
}