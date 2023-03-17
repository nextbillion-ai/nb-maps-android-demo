package ai.nextbillion.utils;

import ai.nextbillion.camera.CameraAnimationDelegate;
import ai.nextbillion.camera.CameraUpdateMode;
import ai.nextbillion.camera.DemoCameraUpdate;
import ai.nextbillion.maps.camera.CameraPosition;
import ai.nextbillion.maps.camera.CameraUpdate;
import ai.nextbillion.maps.camera.CameraUpdateFactory;
import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.geometry.LatLng;
import ai.nextbillion.maps.geometry.LatLngBounds;

public class CameraAnimateUtils {
    private static int DEFAULT_CAMERA_ZOOM = 16;
    private static int CAMERA_ANIMATION_DURATION = 300;

    public  static  void animateCameraBox(
            CameraAnimationDelegate delegate,
            LatLngBounds bounds,
            int animationTime,
            int[] padding
    ) {
        CameraPosition position = delegate.getPosition(bounds, padding);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        DemoCameraUpdate demoCameraUpdate = new DemoCameraUpdate(cameraUpdate);
        demoCameraUpdate.setMode(CameraUpdateMode.OVERRIDE);
        delegate.render(demoCameraUpdate, animationTime, null );
    }

    public static void  animateCameraWithZoom16(NextbillionMap nextbillionMap, LatLng point) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, DEFAULT_CAMERA_ZOOM);
        DemoCameraUpdate demoCameraUpdate = new DemoCameraUpdate(cameraUpdate);
        CameraAnimationDelegate  delegate = new CameraAnimationDelegate(nextbillionMap);
        delegate.render(demoCameraUpdate, CAMERA_ANIMATION_DURATION,null );
    }

    public  static void  animateCamera(NextbillionMap nextbillionMap,LatLng latLng){
        CameraAnimationDelegate  delegate = new CameraAnimationDelegate(nextbillionMap);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        DemoCameraUpdate demoCameraUpdate = new DemoCameraUpdate(cameraUpdate);
        delegate.render(demoCameraUpdate,CAMERA_ANIMATION_DURATION,null);
    }

}
