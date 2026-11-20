package ai.nextbillion;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

/** Animates an ImageSource only while this screen is started and its style is valid. */
public class AnimatedImageSourceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String ID_IMAGE_SOURCE = "animated_image_source";
    private static final String ID_IMAGE_LAYER = "animated_image_layer";
    private MapView mapView;
    private NextbillionMap map;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private RefreshImageRunnable runnable;
    private ImageSource imageSource;
    private Style imageStyle;
    private boolean started;
    private boolean destroyed;
    private final MapView.OnDidFinishLoadingStyleListener styleListener = this::restoreImageSource;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_animated_image_source);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(state);
        // Runtime sources and layers are lost on style replacement; restore them after every load.
        mapView.addOnDidFinishLoadingStyleListener(styleListener);
        mapView.getMapAsync(this);
    }

    @Override public void onMapReady(@NonNull NextbillionMap map) {
        if (destroyed) return;
        this.map = map;
        map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(21.333463356222776, 80.53771145635554))
                .zoom(3.5).bearing(0).tilt(0).build()));
        map.setStyle(new Style.Builder().fromUri(StyleConstants.getBright()));
    }

    private void restoreImageSource() {
        if (destroyed || map == null) return;
        Style style = map.getStyle();
        if (style == null) return;
        // The previous runnable owns a source from the old style. Cancel it before rebinding.
        stopAnimation();
        imageStyle = style;
        imageSource = style.getSourceAs(ID_IMAGE_SOURCE);
        if (imageSource == null) {
            LatLngQuad quad = new LatLngQuad(
                    new LatLng(27.40047198444738, 71.53771145635554),
                    new LatLng(27.40047198444738, 90.94171027114842),
                    new LatLng(15.333463356222776, 90.94171027114842),
                    new LatLng(15.333463356222776, 71.53771145635554));
            imageSource = new ImageSource(ID_IMAGE_SOURCE, quad, R.mipmap.southeast_radar_0);
            style.addSource(imageSource);
        }
        if (style.getLayer(ID_IMAGE_LAYER) == null) {
            style.addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));
        }
        startAnimation();
    }

    // Both lifecycle entry and style loading can request a start; keep only one runnable
    // and verify that its source still belongs to the loaded style.
    private void startAnimation() {
        if (started && !destroyed && runnable == null && imageSource != null
                && imageStyle != null && imageStyle.isFullyLoaded()
                && imageStyle.getSource(ID_IMAGE_SOURCE) == imageSource) {
            runnable = new RefreshImageRunnable(imageSource, imageStyle, handler);
            handler.postDelayed(runnable, 100);
        }
    }

    private void stopAnimation() {
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); started = true; startAnimation(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { started = false; stopAnimation(); mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() {
        destroyed = true;
        stopAnimation();
        handler.removeCallbacksAndMessages(null);
        mapView.removeOnDidFinishLoadingStyleListener(styleListener);
        mapView.onDestroy();
        imageSource = null; imageStyle = null; map = null;
        super.onDestroy();
    }
    @Override protected void onSaveInstanceState(@NonNull Bundle state) { mapView.onSaveInstanceState(state); super.onSaveInstanceState(state); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    // A static runnable avoids an implicit Activity reference. Load bitmaps with the
    // application context and drop bitmap/style/source references when the runnable is cancelled.
    private static class RefreshImageRunnable implements Runnable {
        private ImageSource source;
        private Style style;
        private final Handler handler;
        private Bitmap[] images;
        private int index = 1;
        private boolean canceled;

        RefreshImageRunnable(ImageSource source, Style style, Handler handler) {
            this.source = source; this.style = style; this.handler = handler;
            Context context = Nextbillion.getApplicationContext();
            int[] resources = {R.mipmap.southeast_radar_0, R.mipmap.southeast_radar_1,
                    R.mipmap.southeast_radar_2, R.mipmap.southeast_radar_3};
            images = new Bitmap[resources.length];
            for (int i = 0; i < resources.length; i++) {
                images[i] = BitmapUtils.getBitmapFromDrawable(BitmapUtils.getDrawableFromRes(context, resources[i]));
            }
        }
        void cancel() {
            canceled = true;
            handler.removeCallbacks(this);
            source = null; style = null; images = null;
        }
        @Override public void run() {
            // Style changes can invalidate the source between two scheduled frames.
            if (canceled || style == null || !style.isFullyLoaded()
                    || style.getSource(ID_IMAGE_SOURCE) != source || images[index] == null) {
                cancel();
                return;
            }
            source.setImage(images[index]);
            index = (index + 1) % images.length;
            handler.postDelayed(this, 1000);
        }
    }
}
