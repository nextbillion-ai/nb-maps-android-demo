package ai.nextbillion;

import android.os.Handler;
import android.os.Looper;

import ai.nextbillion.kits.geojson.Point;
import ai.nextbillion.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.maps.plugins.annotation.Symbol;
import ai.nextbillion.maps.plugins.annotation.SymbolManager;

public class RouteAnimator {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int routeIndex = 0;
    private int routeSize;
    private final long duration = 100;

    private final SymbolManager symbolManager;
    private final Symbol vehicleSymbol;
    private final RouteLine mRouteLine;
    private final List<Point> routePoints;
    private final List<LatLng> route = new ArrayList<>();

    private boolean isCanceled = false;
    private RouteAnimatorCallback callback;

    public RouteAnimator(SymbolManager symbolManager, Symbol vehicleSymbol, RouteLine mRouteLine, List<Point> routePoints, RouteAnimatorCallback callback) {
        this.symbolManager = symbolManager;
        this.vehicleSymbol = vehicleSymbol;
        this.mRouteLine = mRouteLine;
        this.routePoints = routePoints;
        for (Point point : routePoints) {
            route.add(new LatLng(point.latitude(), point.longitude()));
        }
        this.callback = callback;
    }

    public void start() {
        routeSize = route.size();
        animateAlongRoute();
    }

    public void cancel() {
        isCanceled = true;
    }

    private void animateAlongRoute() {
        if (isCanceled) {
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCanceled) {
                    return;
                }
                routeIndex += 10;
                if (routeIndex < routeSize) {
                    List<Point> remainingRoute = routePoints.subList(routeIndex, routeSize);
                    LatLng dest = route.get(routeIndex);
                    vehicleSymbol.moveTo(symbolManager, dest, duration, 100);
                    mRouteLine.updateRoute(remainingRoute);
                    animateAlongRoute();
                } else {
                    LatLng dest = route.get(routeSize - 1);
                    vehicleSymbol.moveTo(symbolManager, dest, duration, 100);
                    if (callback != null) {
                        callback.onAnimationEnd();
                    }
                }
            }
        }, duration);
    }

    public interface RouteAnimatorCallback {
        void onAnimationEnd();
    }
}
