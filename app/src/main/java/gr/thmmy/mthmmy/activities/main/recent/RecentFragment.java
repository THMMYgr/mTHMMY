package gr.thmmy.mthmmy.activities.main.recent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Response;
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

    private DocumentSnapshot recentDocument;

    private RecentTask recentTask;

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
        if (recentDocument == null) {
            DocumentReference docRef = BaseApplication.getInstance().getFirestoredb()
                    .collection("recent_posts")
                    .document("recent");
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    recentDocument = task.getResult();
                }
            });
            recentTask = new RecentTask(this::onRecentTaskStarted, this::onRecentTaskFinished);
            recentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.indexUrl.toString());

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
            recentAdapter = new RecentAdapter(getActivity(), (List<DocumentReference>) recentDocument.get("posts"), fragmentInteractionListener);

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
            swipeRefreshLayout.setOnRefreshListener(() -> {
                        if (!recentTask.isRunning()) {
                            recentTask = new RecentTask(this::onRecentTaskStarted, this::onRecentTaskFinished);
                            recentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.indexUrl.toString());
                        }
                    }
            );
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recentTask.isRunning())
            recentTask.cancel(true);
    }


    public interface RecentFragmentInteractionListener extends FragmentInteractionListener {
        void onRecentFragmentInteraction(TopicSummary topicSummary);
    }

    private void onRecentTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void onRecentTaskFinished(int resultCode, ArrayList<TopicSummary> fetchedRecent) {
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            topicSummaries.clear();
            topicSummaries.addAll(fetchedRecent);
            recentAdapter.notifyDataSetChanged();
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Unexpected error," +
                    " please contact the developers with the details", Toast.LENGTH_LONG).show();
        }

        progressBar.setVisibility(ProgressBar.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }
}
