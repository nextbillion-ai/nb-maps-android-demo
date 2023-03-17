package ai.nextbillion.overview;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ai.nextbillion.R;

public class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.OverviewViewHolder> {
    List<DemoFeature> features;

    public OverviewAdapter(List<DemoFeature> features) {
        this.features = features;
    }

    public DemoFeature getFeature(int index) {
        if (index < features.size())
            return features.get(index);
        return null;
    }

    @NonNull
    @Override
    public OverviewAdapter.OverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.overview_item, parent, false);
        return new OverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OverviewAdapter.OverviewViewHolder holder, int position) {
        holder.labelView.setText(features.get(position).label);
        holder.descriptionView.setText(features.get(position).desc);
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    ///////////////////////////////////////////////////////////////////////////
    // View Holder
    ///////////////////////////////////////////////////////////////////////////

    public static class OverviewViewHolder extends RecyclerView.ViewHolder {

        public TextView labelView;
        public TextView descriptionView;

        public OverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            labelView = (TextView) itemView.findViewById(R.id.nameView);
            descriptionView = (TextView) itemView.findViewById(R.id.descriptionView);
        }
    }
}
