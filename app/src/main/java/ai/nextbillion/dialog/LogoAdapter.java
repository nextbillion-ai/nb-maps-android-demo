package ai.nextbillion.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import ai.nextbillion.R;


public class LogoAdapter extends RecyclerView.Adapter<LogoAdapter.ViewHolder> {

    private final LogoInfo[] logoInfoSet;
    LogoOnClickListener clickListener;

    public interface LogoOnClickListener {
        void onSelect(LogoInfo info);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout logoBg;
        private final ImageView logoIv;
        public ViewHolder(View view) {
            super(view);
            logoBg = (RelativeLayout) view.findViewById(R.id.logo_bg);
            logoIv = view.findViewById(R.id.logo_iv);
        }

        public RelativeLayout getBg(){
            return logoBg;
        }

        public ImageView getLogoIv(){
            return logoIv;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public LogoAdapter(LogoInfo[] dataSet) {
        logoInfoSet = dataSet;
    }

    public void setOnclickListener(LogoOnClickListener listener){
        clickListener = listener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.logo_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        LogoInfo logoInfo =  logoInfoSet[position];
        int bgId = logoInfo.selected ? R.drawable.selected_rounded_corner_box : R.drawable.un_selected_rounded_corner_box ;
        viewHolder.getBg().setBackgroundResource(bgId);
        viewHolder.getLogoIv().setImageResource(logoInfo.resourceId);

        viewHolder.getBg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.getBg().setBackgroundResource(R.drawable.selected_rounded_corner_box );
                logoInfo.selected = true;
                clickListener.onSelect(logoInfo);
                notifyDataSetChanged();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return logoInfoSet.length;
    }
}
