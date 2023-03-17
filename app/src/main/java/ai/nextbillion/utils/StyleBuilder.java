package ai.nextbillion.utils;

import androidx.annotation.NonNull;

import ai.nextbillion.maps.core.NextbillionMap;
import ai.nextbillion.maps.core.Style;
import ai.nextbillion.maps.style.sources.VectorSource;

public class StyleBuilder {

    public static Style.Builder buildFromAssets(@NonNull String assetsPath) {
        return new Style.Builder().fromUri("asset://" + assetsPath);
    }

    //https://vt-usa.kroger.nextbillion.io/capabilities/postgres.json
    public static void setDefaultTileService(@NonNull NextbillionMap map, @NonNull String tileServer) {
        map.setStyle(new Style.Builder().fromUri("asset://nb_bright.json"), style -> {
            if (style.getSource("openmaptiles") == null) {
                VectorSource vectorSource = new VectorSource("openmaptiles", tileServer);
                style.addSource(vectorSource);
            }
        });
    }

}
