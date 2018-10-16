package gr.thmmy.mthmmy.activities.shoutbox;

import android.os.Bundle;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;

public class ShoutboxActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoutbox);

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Shoutbox");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(SHOUTBOX_ID);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ShoutboxFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    protected void onResume() {
        drawer.setSelection(SHOUTBOX_ID);
        super.onResume();
    }
}
