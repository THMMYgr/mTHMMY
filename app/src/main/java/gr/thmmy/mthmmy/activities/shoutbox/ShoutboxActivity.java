package gr.thmmy.mthmmy.activities.shoutbox;

import android.os.Bundle;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;

public class ShoutboxActivity extends BaseActivity {

    private ShoutboxFragment shoutboxFragment;

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

        shoutboxFragment = ShoutboxFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, shoutboxFragment)
                .commitNow();
    }

    @Override
    protected void onResume() {
        drawer.setSelection(SHOUTBOX_ID);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            if (!shoutboxFragment.onBackPressed())
                super.onBackPressed();
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
