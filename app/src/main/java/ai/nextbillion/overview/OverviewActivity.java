package ai.nextbillion.overview;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ai.nextbillion.R;
import ai.nextbillion.utils.ItemClickSupport;

public class OverviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OverviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());
        recyclerView.setHasFixedSize(true);

        loadFeatures();
        addItemClickingSupport();
    }

    private void loadFeatures() {
        try {
            List<DemoFeature> featureList = DemoFeature.loadFeatures(this.getApplicationContext());
            adapter = new OverviewAdapter(featureList);
            recyclerView.setAdapter(adapter);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addItemClickingSupport() {
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View view) {
                DemoFeature feature = adapter.getFeature(position);
                if (feature != null)
                    feature.launchFeature(OverviewActivity.this);
            }
        });
    }

}
