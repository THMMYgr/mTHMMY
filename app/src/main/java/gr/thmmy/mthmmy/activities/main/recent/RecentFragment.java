package gr.thmmy.mthmmy.activities.main.recent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
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
    public RecentFragment() {}


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
        if (recentItems == null) {
            Timber.d("I'm ere");
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    DocumentReference docRef = BaseApplication.getInstance().getFirestoredb()
                            .collection("recent_posts")
                            .document("recent");
                    Timber.d("I'm here");
                    docRef.get().addOnCompleteListener(task -> {
                        Timber.d("I'm there");
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        if (task.isSuccessful()) {
                            DocumentSnapshot recentDocument = task.getResult();
                            List<DocumentReference> posts = (List<DocumentReference>) recentDocument.get("posts");
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (DocumentReference documentReference : posts) {
                                Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
                                tasks.add(documentSnapshotTask);
                            }
                            Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
                                ArrayList<RecentItem> recentItems = new ArrayList<>();
                                for (Object object : objects) {
                                    RecentItem recentItem = ((DocumentSnapshot) object).toObject(RecentItem.class);
                                    recentItems.add(recentItem);
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return null;
                }
            }.execute();
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        Timber.d("onActivityCreated");
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
