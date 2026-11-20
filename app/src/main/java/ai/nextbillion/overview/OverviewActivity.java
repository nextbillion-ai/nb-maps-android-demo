package ai.nextbillion.overview;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.ref.WeakReference;

import ai.nextbillion.R;
import ai.nextbillion.kits.geojson.Feature;
import ai.nextbillion.utils.ItemClickSupport;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OverviewActivity extends AppCompatActivity {

    private static final String KEY_STATE_FEATURES = "featureList";

    private LoadFeatureTask loadFeatureTask;
    private RecyclerView recyclerView;
    private OverviewAdapter sectionAdapter;
    private List<DemoFeature> features;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());
        recyclerView.setHasFixedSize(true);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener((recyclerView, position, view) -> {
            if (!sectionAdapter.isSectionHeaderPosition(position)) {
                int itemPosition = sectionAdapter.getConvertedPosition(position);
                DemoFeature feature = features.get(itemPosition);
                startFeature(feature);
            }
        });

        if (savedInstanceState == null) {
            loadFeatures();
        } else {
            features = savedInstanceState.getParcelableArrayList(KEY_STATE_FEATURES);
            if (features == null) loadFeatures(); else onFeaturesLoaded(features);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
    }

    private void loadFeatures() {
        try {
            loadFeatureTask = new LoadFeatureTask(this);
            loadFeatureTask.execute(
                    getPackageManager().getPackageInfo(getPackageName(),
                            PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException exception) {
        }
    }

    private void onFeaturesLoaded(List<DemoFeature> featuresList) {
        features = featuresList;
        if (featuresList == null || featuresList.isEmpty()) {
            return;
        }

        List<OverviewAdapter.Section> sections = new ArrayList<>();
        String currentCat = "";
        for (int i = 0; i < features.size(); i++) {
            String category = features.get(i).getCategory();
            if (!TextUtils.equals(category, currentCat)) {
                sections.add(new OverviewAdapter.Section(i, category));
                currentCat = category;
            }
        }

        OverviewAdapter.Section[] dummy = new OverviewAdapter.Section[sections.size()];
        sectionAdapter = new OverviewAdapter(
                this, R.layout.section_main_layout, R.id.section_text, new FeatureAdapter(features));
        sectionAdapter.setSections(sections.toArray(dummy));
        recyclerView.setAdapter(sectionAdapter);
    }

    private void startFeature(DemoFeature feature) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(getPackageName(), feature.getName()));
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_STATE_FEATURES, (ArrayList<DemoFeature>) features);
    }

    @Override
    protected void onDestroy() {
        if (loadFeatureTask != null) loadFeatureTask.cancel(true);
        super.onDestroy();
    }

    private static class LoadFeatureTask extends AsyncTask<PackageInfo, Void, List<DemoFeature>> {
        private final WeakReference<OverviewActivity> owner;
        private final Resources resources;

        LoadFeatureTask(OverviewActivity activity) {
            owner = new WeakReference<>(activity);
            resources = activity.getApplicationContext().getResources();
        }

        @Override
        protected List<DemoFeature> doInBackground(PackageInfo... params) {
            List<DemoFeature> features = new ArrayList<>();
            PackageInfo app = params[0];
            String metaDataKey = "category";

            for (ActivityInfo info : app.activities) {
                if (isCancelled()) break;
                if (info.labelRes != 0 && !info.name.equals(OverviewActivity.class.getName()) && info.enabled) {
                    String label = resources.getString(info.labelRes);
                    String description = resolveString(info.descriptionRes);
                    String category = resolveMetaData(info.metaData, metaDataKey);
                    features.add(new DemoFeature(info.name, label, description, category));
                }
            }

            if (!features.isEmpty()) {
                Comparator<DemoFeature> comparator = (lhs, rhs) -> {
                    int result = lhs.getCategory().compareToIgnoreCase(rhs.getCategory());
                    if (result == 0) {
                        result = lhs.getLabel().compareToIgnoreCase(rhs.getLabel());
                    }
                    return result;
                };
                Collections.sort(features, comparator);
            }

            return features;
        }

        private String resolveMetaData(Bundle bundle, String key) {
            String category = null;
            if (bundle != null) {
                category = bundle.getString(key);
            }
            return category;
        }

        private String resolveString(@StringRes int stringRes) {
            try {
                return resources.getString(stringRes);
            } catch (Resources.NotFoundException exception) {
                return "-";
            }
        }

        @Override
        protected void onPostExecute(List<DemoFeature> features) {
            super.onPostExecute(features);
            OverviewActivity activity = owner.get();
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                activity.onFeaturesLoaded(features);
            }
        }
    }
}