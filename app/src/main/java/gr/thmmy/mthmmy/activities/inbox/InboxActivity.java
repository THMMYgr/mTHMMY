package gr.thmmy.mthmmy.activities.inbox;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.viewmodel.InboxViewModel;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class InboxActivity extends BaseActivity {

    private InboxViewModel inboxViewModel;

    private MaterialProgressBar progressBar;
    private RecyclerView pmRecyclerview;
    private InboxAdapter inboxAdapter;

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

        inboxViewModel = ViewModelProviders.of(this).get(InboxViewModel.class);
        subscribeUI();

        inboxViewModel.loadInbox();
    }

    private void subscribeUI() {
        inboxViewModel.setOnInboxTaskStartedListener(() -> progressBar.setVisibility(View.VISIBLE));
        inboxViewModel.setOnInboxTaskFinishedListener((resultCode, inbox) -> {
            progressBar.setVisibility(View.INVISIBLE);
            if (resultCode == NetworkResultCodes.SUCCESSFUL) {
                Timber.i("Successfully loaded inbox");
                inboxAdapter.notifyDataSetChanged();
            } else {

            }
        });
    }
}
