package ai.nextbillion;

import android.app.Application;

import ai.nextbillion.maps.Nextbillion;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Nextbillion.getInstance(this, getString(R.string.nbmap_access_key));
    }
}
