package ai.nextbillion;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.gestures.AndroidGesturesManager;
import ai.nextbillion.gestures.MoveGestureDetector;
import ai.nextbillion.gestures.RotateGestureDetector;
import ai.nextbillion.gestures.ShoveGestureDetector;
import ai.nextbillion.gestures.StandardScaleGestureDetector;
import ai.nextbillion.maps.annotations.Marker;
import ai.nextbillion.maps.annotations.MarkerOptions;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.MapView;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.UiSettings;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.utils.FontCache;
import ai.nextbillion.utils.ResourceUtils;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import ai.nextbillion.R;
/**
 * Test activity showcasing APIs around gestures implementation.
 */
public class GestureDetectorActivity extends AppCompatActivity {

    private static final int MAX_NUMBER_OF_ALERTS = 30;

    private MapView mapView;
    private NextbillionMap nextbillionMap;
    private RecyclerView recyclerView;
    private GestureAlertsAdapter gestureAlertsAdapter;

    private AndroidGesturesManager gesturesManager;

    @Nullable
    private Marker marker;
    @Nullable
    private LatLng focalPointLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_detector);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(nextbillionMap -> {
            GestureDetectorActivity.this.nextbillionMap = nextbillionMap;
            nextbillionMap.setStyle(StyleConstants.NBMAP_STREETS);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(22.70418008712976, 78.66264025041812))
                    .zoom(6)
                    .tilt(30)
                    .tilt(0)
                    .build();
            nextbillionMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            initializeMap();
        });

        recyclerView = findViewById(R.id.alerts_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        gestureAlertsAdapter = new GestureAlertsAdapter();
        recyclerView.setAdapter(gestureAlertsAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gestureAlertsAdapter.cancelUpdates();
        mapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

    private void initializeMap() {
        gesturesManager = nextbillionMap.getGesturesManager();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
        layoutParams.height = (int) (mapView.getHeight() / 1.75);
        layoutParams.width = (mapView.getWidth() / 3);
        recyclerView.setLayoutParams(layoutParams);

        attachListeners();

        fixedFocalPointEnabled(nextbillionMap.getUiSettings().getFocalPoint() != null);
    }

    public void attachListeners() {
        nextbillionMap.addOnMoveListener(new NextbillionMap.OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_START, "MOVE START"));
            }

            @Override
            public void onMove(@NonNull MoveGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_PROGRESS, "MOVE PROGRESS"));
            }

            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_END, "MOVE END"));
                recalculateFocalPoint();
            }
        });

        nextbillionMap.addOnRotateListener(new NextbillionMap.OnRotateListener() {
            @Override
            public void onRotateBegin(@NonNull RotateGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_START, "ROTATE START"));
            }

            @Override
            public void onRotate(@NonNull RotateGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_PROGRESS, "ROTATE PROGRESS"));
                recalculateFocalPoint();
            }

            @Override
            public void onRotateEnd(@NonNull RotateGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_END, "ROTATE END"));
            }
        });

        nextbillionMap.addOnScaleListener(new NextbillionMap.OnScaleListener() {
            @Override
            public void onScaleBegin(@NonNull StandardScaleGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_START, "SCALE START"));
                if (focalPointLatLng != null) {
                    gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_OTHER, "INCREASING MOVE THRESHOLD"));
                    gesturesManager.getMoveGestureDetector().setMoveThreshold(
                            ResourceUtils.convertDpToPx(GestureDetectorActivity.this, 175));

                    gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_OTHER, "MANUALLY INTERRUPTING MOVE"));
                    gesturesManager.getMoveGestureDetector().interrupt();
                }
                recalculateFocalPoint();
            }

            @Override
            public void onScale(@NonNull StandardScaleGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_PROGRESS, "SCALE PROGRESS"));
            }

            @Override
            public void onScaleEnd(@NonNull StandardScaleGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_END, "SCALE END"));

                if (focalPointLatLng != null) {
                    gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_OTHER, "REVERTING MOVE THRESHOLD"));
                    gesturesManager.getMoveGestureDetector().setMoveThreshold(0f);
                }
            }
        });

        nextbillionMap.addOnShoveListener(new NextbillionMap.OnShoveListener() {
            @Override
            public void onShoveBegin(@NonNull ShoveGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_START, "SHOVE START"));
            }

            @Override
            public void onShove(@NonNull ShoveGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_PROGRESS, "SHOVE PROGRESS"));
            }

            @Override
            public void onShoveEnd(@NonNull ShoveGestureDetector detector) {
                gestureAlertsAdapter.addAlert(new GestureAlert(GestureAlert.TYPE_END, "SHOVE END"));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gestures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UiSettings uiSettings = nextbillionMap.getUiSettings();
        if (item.getItemId() == R.id.menu_gesture_focus_point) {
            fixedFocalPointEnabled(focalPointLatLng == null);
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_animation) {
            uiSettings.setScaleVelocityAnimationEnabled(!uiSettings.isScaleVelocityAnimationEnabled());
            uiSettings.setRotateVelocityAnimationEnabled(!uiSettings.isRotateVelocityAnimationEnabled());
            uiSettings.setFlingVelocityAnimationEnabled(!uiSettings.isFlingVelocityAnimationEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_rotate) {
            uiSettings.setRotateGesturesEnabled(!uiSettings.isRotateGesturesEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_tilt) {
            uiSettings.setTiltGesturesEnabled(!uiSettings.isTiltGesturesEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_zoom) {
            uiSettings.setZoomGesturesEnabled(!uiSettings.isZoomGesturesEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_scroll) {
            uiSettings.setScrollGesturesEnabled(!uiSettings.isScrollGesturesEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_double_tap) {
            uiSettings.setDoubleTapGesturesEnabled(!uiSettings.isDoubleTapGesturesEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_quick_zoom) {
            uiSettings.setQuickZoomGesturesEnabled(!uiSettings.isQuickZoomGesturesEnabled());
            return true;
        } else if (item.getItemId() == R.id.menu_gesture_scroll_horizontal) {
            uiSettings.setHorizontalScrollGesturesEnabled(!uiSettings.isHorizontalScrollGesturesEnabled());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fixedFocalPointEnabled(boolean enabled) {
        if (enabled) {
            focalPointLatLng = new LatLng(51.50325, -0.12968);
            marker = nextbillionMap.addMarker(new MarkerOptions().position(focalPointLatLng));
            nextbillionMap.easeCamera(CameraUpdateFactory.newLatLngZoom(focalPointLatLng, 16),
                    new NextbillionMap.CancelableCallback() {
                        @Override
                        public void onCancel() {
                            recalculateFocalPoint();
                        }

                        @Override
                        public void onFinish() {
                            recalculateFocalPoint();
                        }
                    });
        } else {
            if (marker != null) {
                nextbillionMap.removeMarker(marker);
                marker = null;
            }
            focalPointLatLng = null;
            nextbillionMap.getUiSettings().setFocalPoint(null);
        }
    }

    private void recalculateFocalPoint() {
        if (focalPointLatLng != null) {
            nextbillionMap.getUiSettings().setFocalPoint(
                    nextbillionMap.getProjection().toScreenLocation(focalPointLatLng)
            );
        }
    }

    private static class GestureAlertsAdapter extends RecyclerView.Adapter<GestureAlertsAdapter.ViewHolder> {

        private boolean isUpdating;
        private final Handler updateHandler = new Handler();
        private final List<GestureAlert> alerts = new ArrayList<>();

        public static class ViewHolder extends RecyclerView.ViewHolder {

            TextView alertMessageTv;

            @ColorInt
            public int textColor;

            ViewHolder(View view) {
                super(view);
                Typeface typeface = FontCache.get("Roboto-Regular.ttf", view.getContext());
                alertMessageTv = view.findViewById(R.id.alert_message);
                alertMessageTv.setTypeface(typeface);
            }
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gesture_alert, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GestureAlert alert = alerts.get(position);
            holder.alertMessageTv.setText(alert.getMessage());
            holder.alertMessageTv.setTextColor(
                    ContextCompat.getColor(holder.alertMessageTv.getContext(), alert.getColor()));
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }

        void addAlert(GestureAlert alert) {
            for (GestureAlert gestureAlert : alerts) {
                if (gestureAlert.getAlertType() != GestureAlert.TYPE_PROGRESS) {
                    break;
                }

                if (alert.getAlertType() == GestureAlert.TYPE_PROGRESS && gestureAlert.equals(alert)) {
                    return;
                }
            }

            if (getItemCount() >= MAX_NUMBER_OF_ALERTS) {
                alerts.remove(getItemCount() - 1);
            }

            alerts.add(0, alert);
            if (!isUpdating) {
                isUpdating = true;
                updateHandler.postDelayed(updateRunnable, 250);
            }
        }

        private Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                isUpdating = false;
            }
        };

        void cancelUpdates() {
            updateHandler.removeCallbacksAndMessages(null);
        }
    }

    private static class GestureAlert {
        @Retention(SOURCE)
        @IntDef( {TYPE_NONE, TYPE_START, TYPE_PROGRESS, TYPE_END, TYPE_OTHER})
        @interface Type {
        }

        static final int TYPE_NONE = 0;
        static final int TYPE_START = 1;
        static final int TYPE_END = 2;
        static final int TYPE_PROGRESS = 3;
        static final int TYPE_OTHER = 4;

        @Type
        private int alertType;

        private String message;

        @ColorInt
        private int color;

        GestureAlert(@Type int alertType, String message) {
            this.alertType = alertType;
            this.message = message;

            switch (alertType) {
                case TYPE_NONE:
                    color = android.R.color.black;
                    break;
                case TYPE_END:
                    color = android.R.color.holo_red_dark;
                    break;
                case TYPE_OTHER:
                    color = android.R.color.holo_purple;
                    break;
                case TYPE_PROGRESS:
                    color = android.R.color.holo_orange_dark;
                    break;
                case TYPE_START:
                    color = android.R.color.holo_green_dark;
                    break;
            }
        }

        int getAlertType() {
            return alertType;
        }

        String getMessage() {
            return message;
        }

        int getColor() {
            return color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            GestureAlert that = (GestureAlert) o;

            if (alertType != that.alertType) {
                return false;
            }
            return message != null ? message.equals(that.message) : that.message == null;
        }

        @Override
        public int hashCode() {
            int result = alertType;
            result = 31 * result + (message != null ? message.hashCode() : 0);
            return result;
        }
    }
}
