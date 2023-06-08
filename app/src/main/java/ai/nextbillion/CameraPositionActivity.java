package ai.nextbillion;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.OnMapReadyCallback;
import ai.nextbillion.maps.geometry.LatLng;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static ai.nextbillion.maps.constants.GeometryConstants.MAX_LATITUDE;
import static ai.nextbillion.maps.constants.GeometryConstants.MIN_LATITUDE;

/**
 * Test activity showcasing how to listen to camera change events.
 */
public class CameraPositionActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener,
        NextbillionMap.OnMapLongClickListener {

    private MapView mapView;
    private NextbillionMap nextbillionMap;
    private FloatingActionButton fab;
    private boolean logCameraChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_position);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final NextbillionMap map) {
        nextbillionMap = map;
        map.setStyle(StyleConstants.LIGHT, style -> {
            // add a listener to FAB
            fab = findViewById(R.id.fab);
            fab.setColorFilter(ContextCompat.getColor(CameraPositionActivity.this, R.color.palette_mint_100));
            fab.setOnClickListener(this);

            toggleLogCameraChanges();

            // listen to long click events to toggle logging camera changes
            nextbillionMap.addOnMapLongClickListener(this);
        });
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        toggleLogCameraChanges();
        return false;
    }

    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        final View dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_camera_position, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_camera_position);
        builder.setView(onInflateDialogContent(dialogContent));
        builder.setPositiveButton("Animate", new DialogClickListener(nextbillionMap, dialogContent));
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);
        builder.show();
    }

    private void toggleLogCameraChanges() {
        logCameraChanges = !logCameraChanges;
        if (logCameraChanges) {
            nextbillionMap.addOnCameraIdleListener(idleListener);
            nextbillionMap.addOnCameraMoveCancelListener(moveCanceledListener);
            nextbillionMap.addOnCameraMoveListener(moveListener);
            nextbillionMap.addOnCameraMoveStartedListener(moveStartedListener);
        } else {
            nextbillionMap.removeOnCameraIdleListener(idleListener);
            nextbillionMap.removeOnCameraMoveCancelListener(moveCanceledListener);
            nextbillionMap.removeOnCameraMoveListener(moveListener);
            nextbillionMap.removeOnCameraMoveStartedListener(moveStartedListener);
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (nextbillionMap != null) {
            nextbillionMap.removeOnMapLongClickListener(this);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private View onInflateDialogContent(View view) {
        linkTextView(view, R.id.value_lat, R.id.seekbar_lat, new LatLngChangeListener(), 180 + 38);
        linkTextView(view, R.id.value_lon, R.id.seekbar_lon, new LatLngChangeListener(), 180 - 77);
        linkTextView(view, R.id.value_zoom, R.id.seekbar_zoom, new ValueChangeListener(), 6);
        linkTextView(view, R.id.value_bearing, R.id.seekbar_bearing, new ValueChangeListener(), 90);
        linkTextView(view, R.id.value_tilt, R.id.seekbar_tilt, new ValueChangeListener(), 40);
        return view;
    }

    private void linkTextView(
            View view, @IdRes int textViewRes, @IdRes int seekBarRes, ValueChangeListener listener, int defaultValue) {
        final TextView value = (TextView) view.findViewById(textViewRes);
        SeekBar seekBar = (SeekBar) view.findViewById(seekBarRes);
        listener.setLinkedValueView(value);
        seekBar.setOnSeekBarChangeListener(listener);
        seekBar.setProgress(defaultValue);
    }

    private NextbillionMap.OnCameraIdleListener idleListener = new NextbillionMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            fab.setColorFilter(ContextCompat.getColor(CameraPositionActivity.this, android.R.color.holo_green_dark));
        }
    };

    private NextbillionMap.OnCameraMoveListener moveListener = new NextbillionMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
            fab.setColorFilter(ContextCompat.getColor(CameraPositionActivity.this, android.R.color.holo_orange_dark));
        }
    };

    private NextbillionMap.OnCameraMoveCanceledListener moveCanceledListener = () -> {};

    private NextbillionMap.OnCameraMoveStartedListener moveStartedListener = new NextbillionMap.OnCameraMoveStartedListener() {

        private final String[] REASONS = {"REASON_API_GESTURE", "REASON_DEVELOPER_ANIMATION", "REASON_API_ANIMATION"};

        @Override
        public void onCameraMoveStarted(int reason) {
            // reason ranges from 1 <-> 3
            fab.setColorFilter(ContextCompat.getColor(CameraPositionActivity.this, android.R.color.holo_red_dark));
        }
    };

    private class ValueChangeListener implements SeekBar.OnSeekBarChangeListener {

        protected TextView textView;

        public void setLinkedValueView(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            textView.setText(String.valueOf(progress));
        }
    }

    private class LatLngChangeListener extends ValueChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            super.onProgressChanged(seekBar, progress - 180, fromUser);
        }
    }

    private static class DialogClickListener implements DialogInterface.OnClickListener {

        private NextbillionMap nextbillionMap;
        private View dialogContent;

        public DialogClickListener(NextbillionMap nextbillionMap, View view) {
            this.nextbillionMap = nextbillionMap;
            this.dialogContent = view;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            double latitude = Double.parseDouble(
                    ((TextView) dialogContent.findViewById(R.id.value_lat)).getText().toString());
            double longitude = Double.parseDouble(
                    ((TextView) dialogContent.findViewById(R.id.value_lon)).getText().toString());
            double zoom = Double.parseDouble(
                    ((TextView) dialogContent.findViewById(R.id.value_zoom)).getText().toString());
            double bearing = Double.parseDouble(
                    ((TextView) dialogContent.findViewById(R.id.value_bearing)).getText().toString());
            double tilt = Double.parseDouble(
                    ((TextView) dialogContent.findViewById(R.id.value_tilt)).getText().toString());

            if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
                Toast.makeText(dialogContent.getContext(), "latitude value must be set "
                                + " between " + MIN_LATITUDE + " and " + MAX_LATITUDE,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(zoom)
                    .bearing(bearing)
                    .tilt(tilt)
                    .build();

            nextbillionMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000,
                    new NextbillionMap.CancelableCallback() {
                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onFinish() {
                        }
                    });
        }
    }
}
