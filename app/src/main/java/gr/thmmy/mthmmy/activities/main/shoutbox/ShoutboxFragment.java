package gr.thmmy.mthmmy.activities.main.shoutbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ShoutboxFragment extends BaseFragment {

    private static final String TAG = "ShoutboxFragment";

    private MaterialProgressBar progressBar;
    private ShoutboxTask shoutboxTask;
    private ShoutAdapter shoutAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Shout> shouts;

    public static ShoutboxFragment newInstance(int sectionNumber) {
        ShoutboxFragment fragment = new ShoutboxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private void onShoutboxTaskSarted() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onShoutboxTaskFinished(int resultCode, ArrayList<Shout> shouts) {
        progressBar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            this.shouts.clear();
            this.shouts.addAll(shouts);
            shoutAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shoutbox, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        CustomRecyclerView recyclerView = rootView.findViewById(R.id.shoutbox_recyclerview);
        shouts = new ArrayList<>();
        shoutAdapter = new ShoutAdapter(getContext(), shouts);
        recyclerView.setAdapter(shoutAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shoutboxTask = new ShoutboxTask(this::onShoutboxTaskSarted, this::onShoutboxTaskFinished);
        shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?");

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            shoutboxTask = new ShoutboxTask(ShoutboxFragment.this::onShoutboxTaskSarted, ShoutboxFragment.this::onShoutboxTaskFinished);
            shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?");
        });

        return rootView;
    }
}
