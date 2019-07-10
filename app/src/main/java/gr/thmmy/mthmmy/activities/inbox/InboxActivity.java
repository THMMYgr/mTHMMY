package gr.thmmy.mthmmy.activities.inbox;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.viewmodel.InboxViewModel;

public class InboxActivity extends BaseActivity {

    InboxViewModel inboxViewModel;

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

        inboxViewModel = ViewModelProviders.of(this).get(InboxViewModel.class);
        subscribeUI();
    }

    private void subscribeUI() {
        inboxViewModel.setOnInboxTaskFinishedListener((resultCode, data) -> {

        });
    }
}
