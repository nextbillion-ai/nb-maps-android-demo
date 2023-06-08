package ai.nextbillion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import ai.nextbillion.maps.Nextbillion;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.geometry.LatLngQuad;
import ai.nextbillion.maps.style.layers.RasterLayer;
import ai.nextbillion.maps.style.sources.ImageSource;
import ai.nextbillion.maps.utils.BitmapUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Test activity showing how to use a series of images to create an animation
 * with an ImageSource
 */
public class AnimatedImageSourceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String ID_IMAGE_SOURCE = "animated_image_source";
    private static final String ID_IMAGE_LAYER = "animated_image_layer";

    private MapView mapView;
    private final Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animated_image_source);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final NextbillionMap map) {
        LatLngQuad quad = new LatLngQuad(
                new LatLng(27.40047198444738, 71.53771145635554),
                new LatLng(27.40047198444738, 90.94171027114842),
                new LatLng(15.333463356222776, 90.94171027114842),
                new LatLng(15.333463356222776, 71.53771145635554)
        );

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(21.333463356222776, 80.53771145635554))
                .zoom(3.5)
                .bearing(0)
                .tilt(0)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position));

        final ImageSource imageSource = new ImageSource(ID_IMAGE_SOURCE, quad, R.mipmap.southeast_radar_0);
        final RasterLayer layer = new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE);
        map.setStyle(new Style.Builder()
                        .fromUri("https://api.nextbillion.io/maps/streets/style.json")
                        .withSource(imageSource)
                        .withLayer(layer), style -> {
                    runnable = new RefreshImageRunnable(imageSource, handler);
                    handler.postDelayed(runnable, 100);
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private static class RefreshImageRunnable implements Runnable {

        private ImageSource imageSource;
        private Handler handler;
        private Bitmap[] drawables;
        private int drawableIndex;

        Bitmap getBitmap(int resourceId) {
            Context context = Nextbillion.getApplicationContext();
            Drawable drawable = BitmapUtils.getDrawableFromRes(context, resourceId);
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                return bitmapDrawable.getBitmap();
            }
            return null;
        }

        RefreshImageRunnable(ImageSource imageSource, Handler handler) {
            this.imageSource = imageSource;
            this.handler = handler;
            drawables = new Bitmap[4];
            drawables[0] = getBitmap(R.mipmap.southeast_radar_0);
            drawables[1] = getBitmap(R.mipmap.southeast_radar_1);
            drawables[2] = getBitmap(R.mipmap.southeast_radar_2);
            drawables[3] = getBitmap(R.mipmap.southeast_radar_3);
            drawableIndex = 1;
        }

        @Override
        public void run() {
            imageSource.setImage(drawables[drawableIndex++]);
            if (drawableIndex > 3) {
                drawableIndex = 0;
            }
            handler.postDelayed(this, 1000);
        }
    }
}
