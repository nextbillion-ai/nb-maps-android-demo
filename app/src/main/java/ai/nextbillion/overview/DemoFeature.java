package ai.nextbillion.overview;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

public class DemoFeature {
    public DemoFeature(String name, String label, String desc) {
        this.name = name;
        this.label = label;
        this.desc = desc;
    }

    String name;
    String label;
    String desc;

    public void launchFeature(Context context) {
        Intent featureIntent = new Intent();
        featureIntent.setComponent(new ComponentName(context.getPackageName(), name));
        context.startActivity(featureIntent);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public static List<DemoFeature> loadFeatures(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
        List<DemoFeature> features = new ArrayList<>();
        String packageName = context.getPackageName();
        for (ActivityInfo info : packageInfo.activities) {
            if (info.labelRes != 0 && info.enabled) {
                features.add(new DemoFeature(info.name, context.getString(info.labelRes), resolveString(info.descriptionRes, context)));
            }
        }
        return features;
    }

    private static String resolveString(@StringRes int stringRes, Context context) {
        try {
            return context.getString(stringRes);
        } catch (Resources.NotFoundException exception) {
            return "-";
        }
    }
}
