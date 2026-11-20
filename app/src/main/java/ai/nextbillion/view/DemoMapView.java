package ai.nextbillion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import ai.nextbillion.maps.core.MapView;

/**
 * Customizes the logo through MapView's protected view-creation hook.
 * Existing examples use this subclass to set their logo while using the current SDK API.
 */
public class DemoMapView extends MapView {
    private ImageView customLogoView;
    private int logoResource;

    public DemoMapView(Context context) {
        super(context);
    }

    public DemoMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Let MapView create and position the logo, then change only its image resource.
    @Override
    protected ImageView initialiseLogoView() {
        customLogoView = super.initialiseLogoView();
        if (logoResource != 0) {
            customLogoView.setImageResource(logoResource);
        }
        return customLogoView;
    }

    public void setLogoResourceId(int resource) {
        // Remember the choice even if the logo view has not been created yet.
        logoResource = resource;
        if (customLogoView != null) {
            customLogoView.setImageResource(resource);
        }
    }
}
