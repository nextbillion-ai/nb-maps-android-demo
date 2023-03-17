package ai.nextbillion.camera;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.NextbillionMap.CancelableCallback;
import ai.nextbillion.maps.geometry.LatLngBounds;
import ai.nextbillion.maps.location.modes.CameraMode;

public class CameraAnimationDelegate {
    private final NextbillionMap nextbillionMap;

    public CameraAnimationDelegate(NextbillionMap nextbillionMap) {
        this.nextbillionMap = nextbillionMap;
    }

    public CameraPosition getPosition(LatLngBounds bounds, int[]  padding){
        return nextbillionMap.getCameraForLatLngBounds(bounds, padding);
    }


    public void render(DemoCameraUpdate update, int durationMs, CancelableCallback callback) {
        CameraUpdateMode mode = update.getMode();
        CameraUpdate cameraUpdate = update.getCameraUpdate();
        if (mode == CameraUpdateMode.OVERRIDE) {
//            nextbillionMap.getLocationComponent().setCameraMode(CameraMode.NONE);
            this.nextbillionMap.animateCamera(cameraUpdate, durationMs, callback);
        } else if (!this.isTracking()) {
            this.nextbillionMap.animateCamera(cameraUpdate, durationMs, callback);
        }

    }

    private boolean isTracking() {
        return false;
//        int cameraMode = this.nextbillionMap.getLocationComponent().getCameraMode();
//        return cameraMode != 8;
    }

}
