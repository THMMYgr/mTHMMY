package gr.thmmy.mthmmy.activities.main.recent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.RecentItem;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;


/**
 * A {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecentFragment.RecentFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentFragment extends BaseFragment {
    private static final String TAG = "RecentFragment";
    // Fragment initialization parameters, e.g. ARG_SECTION_NUMBER

    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecentAdapter recentAdapter;

    private ArrayList<RecentItem> recentItems = new ArrayList<>();

    // Required empty public constructor
    public RecentFragment() {
    }


    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Recent.
     */
    public static RecentFragment newInstance(int sectionNumber) {
        RecentFragment fragment = new RecentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("recent_posts")
                .document("recent");
        docRef.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (task.isSuccessful()) {
                DocumentSnapshot recentDocument = task.getResult();
                ArrayList<HashMap<String, Object>> posts = (ArrayList<HashMap<String, Object>>) recentDocument.get("posts");
                for (HashMap<String, Object> map : posts) {
                    RecentItem recentItem = new RecentItem(map);
                    recentItems.add(recentItem);
                }
                recentAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_recent, container, false);

        // Set the adapter
        if (rootView instanceof RelativeLayout) {
            progressBar = rootView.findViewById(R.id.progressBar);
            recentAdapter = new RecentAdapter(getActivity(), recentItems, fragmentInteractionListener);

            CustomRecyclerView recyclerView = rootView.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(recentAdapter);

            swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
            swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        }

        return rootView;
    }


    public interface RecentFragmentInteractionListener extends FragmentInteractionListener {
        void onRecentFragmentInteraction(RecentItem topicSummary);
    }
}
