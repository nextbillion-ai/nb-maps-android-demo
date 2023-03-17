package ai.nextbillion;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.snapshotter.MapSnapshot;
import ai.nextbillion.maps.snapshotter.MapSnapshotter;

public class SnapshotActivity extends AppCompatActivity implements MapSnapshotter.SnapshotReadyCallback {

    private MapSnapshotter snapshotter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snapshot_activity);

        snapshotter = new MapSnapshotter(getApplicationContext(),
                new MapSnapshotter.Options(516, 516)
                        .withCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(12.97551913, 77.58917229))
                                        .zoom(15)
                                        .build()));
        snapshotter.start(this);

    }

    private void snapshot() {

    }

    @Override
    public void onSnapshotReady(MapSnapshot mapSnapshot) {
        ImageView imageView = findViewById(R.id.snapshot);
        imageView.setImageBitmap(mapSnapshot.getBitmap());
    }

    @Override
    protected void onStop() {
        super.onStop();
        snapshotter.cancel();
    }
}
