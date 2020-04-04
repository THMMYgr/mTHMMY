package gr.thmmy.mthmmy.activities.inbox;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.pagination.BottomPaginationView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.viewmodel.InboxViewModel;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class InboxActivity extends BaseActivity {

    private InboxViewModel inboxViewModel;

    private MaterialProgressBar progressBar;
    private RecyclerView pmRecyclerview;
    private InboxAdapter inboxAdapter;
    private BottomPaginationView bottomPagination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Inbox");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(INBOX_ID);

        progressBar = findViewById(R.id.progress_bar);
        pmRecyclerview = findViewById(R.id.inbox_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        pmRecyclerview.setLayoutManager(layoutManager);
        inboxAdapter = new InboxAdapter(this);
        pmRecyclerview.setAdapter(inboxAdapter);
        bottomPagination = findViewById(R.id.bottom_pagination);

        inboxViewModel = new ViewModelProvider(this).get(InboxViewModel.class);
        bottomPagination.setOnPageRequestedListener(inboxViewModel);
        subscribeUI();

        inboxViewModel.loadInbox();
    }

    private void subscribeUI() {
        inboxViewModel.setOnInboxTaskStartedListener(() -> {
            progressBar.setVisibility(View.VISIBLE);
            Timber.d("inbox task started");
        });
        inboxViewModel.setOnInboxTaskFinishedListener((resultCode, inbox) -> {
            progressBar.setVisibility(View.INVISIBLE);
            if (resultCode == NetworkResultCodes.SUCCESSFUL) {
                Timber.i("Successfully loaded inbox");
                inboxAdapter.notifyDataSetChanged();
            } else {
                Timber.w("Failed to load inbox");
                Toast.makeText(this, "Failed to load inbox", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        inboxViewModel.setOnInboxTaskCancelledListener(() -> {
            progressBar.setVisibility(ProgressBar.GONE);
            Timber.d("inbox task cancelled");
        });
        inboxViewModel.getPageIndicatorIndex().observe(this, pageIndicatorIndex -> {
            if (pageIndicatorIndex == null) return;
            bottomPagination.setIndicatedPageIndex(pageIndicatorIndex);
        });
        inboxViewModel.getPageCount().observe(this, pageCount -> {
            if (pageCount == null) return;
            bottomPagination.setTotalPageCount(pageCount);
        });
    }
}
