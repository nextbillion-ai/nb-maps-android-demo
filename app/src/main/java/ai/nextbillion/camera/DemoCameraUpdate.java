package ai.nextbillion.camera;

import androidx.annotation.NonNull;

import ai.nextbillion.maps.camera.CameraUpdate;


public class DemoCameraUpdate {
    private final CameraUpdate cameraUpdate;
    private CameraUpdateMode mode;

    public DemoCameraUpdate(@NonNull CameraUpdate cameraUpdate) {
        this.mode = CameraUpdateMode.DEFAULT;
        this.cameraUpdate = cameraUpdate;
    }

    public void setMode(CameraUpdateMode mode) {
        this.mode = mode;
    }

    @NonNull
    public CameraUpdate getCameraUpdate() {
        return this.cameraUpdate;
    }

    @NonNull
    public CameraUpdateMode getMode() {
        return this.mode;
    }
}
