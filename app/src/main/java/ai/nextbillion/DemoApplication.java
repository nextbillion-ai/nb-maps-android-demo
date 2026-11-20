package ai.nextbillion;

import android.app.Application;

import ai.nextbillion.maps.Nextbillion;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the SDK before any Activity creates a MapView. Replace the placeholder
        // nbmap_access_key in res/values/strings.xml with your own API key before running.
        Nextbillion.getInstance(this, getString(R.string.nbmap_access_key));
    }
}
