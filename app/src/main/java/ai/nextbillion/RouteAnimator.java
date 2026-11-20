package ai.nextbillion;

import android.os.Handler;
import android.os.Looper;
import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.utils.MapSymbol;
import java.util.ArrayList;
import java.util.List;

/**
 * Advances a vehicle along sampled route points and shortens the remaining route line.
 * All calls must run on the map's UI thread. The Activity should cancel when it stops
 * and close its MapSymbol separately when it is destroyed.
 */
public class RouteAnimator {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long duration = 100;
    private final MapSymbol vehicleSymbol;
    private final RouteLine routeLine;
    private final List<Point> routePoints;
    private final RouteAnimatorCallback callback;
    private int routeIndex;
    private boolean canceled = true;
    // Reuse owned callbacks so restart and cancel can remove all pending work reliably.
    private final Runnable nextStep = this::animateNextPoint;
    private final Runnable completion = this::complete;

    private void complete() {
        if (!canceled) {
            canceled = true;
            if (callback != null) callback.onAnimationEnd();
        }
    }

    public RouteAnimator(MapSymbol vehicleSymbol, RouteLine routeLine, List<Point> routePoints,
                         RouteAnimatorCallback callback) {
        this.vehicleSymbol = vehicleSymbol;
        this.routeLine = routeLine;
        // Keep a snapshot so edits to the caller's list cannot invalidate routeIndex.
        this.routePoints = new ArrayList<>(routePoints);
        this.callback = callback;
    }

    /** Restarts from the beginning; an empty route completes immediately. */
    public void start() {
        cancel();
        routeIndex = 0;
        canceled = false;
        if (routePoints.isEmpty()) {
            completion.run();
        } else if (vehicleSymbol.isAttached()) {
            handler.postDelayed(nextStep, duration);
        } else {
            canceled = true;
        }
    }

    public void cancel() {
        canceled = true;
        // This Handler belongs only to this animator, so clearing its queue is safe.
        // Cancel the in-flight movement as well as the next scheduled route step.
        handler.removeCallbacksAndMessages(null);
        vehicleSymbol.cancel();
    }

    private void animateNextPoint() {
        if (canceled || !vehicleSymbol.isAttached()) {
            cancel();
            return;
        }
        // Skip ten points per step for the demo, clamping to also support a one-point route.
        routeIndex = Math.min(routeIndex + 10, routePoints.size() - 1);
        Point point = routePoints.get(routeIndex);
        vehicleSymbol.moveTo(new LatLng(point.latitude(), point.longitude()), duration);
        if (routeIndex < routePoints.size() - 1) {
            routeLine.updateRoute(routePoints.subList(routeIndex, routePoints.size()));
            handler.postDelayed(nextStep, duration);
        } else {
            // Report completion after the final movement has had time to finish.
            handler.postDelayed(completion, duration);
        }
    }

    public interface RouteAnimatorCallback {
        void onAnimationEnd();
    }
}
