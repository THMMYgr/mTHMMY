package gr.thmmy.mthmmy.activities.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;

public class SettingsActivity extends BaseActivity {
    public static final String DEFAULT_HOME_TAB = "pref_app_main_default_tab_key";
    public static final String NOTIFICATION_LED_KEY = "pref_notification_led_enable_key";
    public static final String NOTIFICATION_VIBRATION_KEY = "pref_notification_vibration_enable_key";
    public static final String APP_SIGNATURE_ENABLE_KEY = "pref_posting_app_signature_enable_key";

    private Fragment preferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(SETTINGS_ID);

        preferenceFragment = SettingsFragment.newInstance(sessionManager.isLoggedIn());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.pref_container, preferenceFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        drawer.setSelection(SETTINGS_ID);
        super.onResume();
        ((SettingsFragment) preferenceFragment).updateUserLoginState(sessionManager.isLoggedIn());
    }
}
