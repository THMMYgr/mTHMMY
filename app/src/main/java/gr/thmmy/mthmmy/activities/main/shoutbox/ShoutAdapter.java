package gr.thmmy.mthmmy.activities.main.shoutbox;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;

public class ShoutAdapter extends CustomRecyclerView.Adapter<ShoutAdapter.ShoutViewHolder> {
    private ArrayList<Shout> shouts;

    public ShoutAdapter(ArrayList<Shout> shouts) {
        this.shouts = shouts;
    }

    @NonNull
    @Override
    public ShoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ShoutViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return shouts.size();
    }

    static class ShoutViewHolder extends CustomRecyclerView.ViewHolder {

        public ShoutViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
