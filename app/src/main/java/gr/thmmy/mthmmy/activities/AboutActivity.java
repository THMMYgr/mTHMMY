package gr.thmmy.mthmmy.activities;

import android.os.Bundle;
import android.widget.TextView;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionName = BuildConfig.VERSION_NAME;
        TextView tv = (TextView) findViewById(R.id.version);
        if (tv != null)
            tv.setText(getString(R.string.version, versionName));

        //TODO: add licenses
    }
}
