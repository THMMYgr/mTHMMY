package gr.thmmy.mthmmy.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.DEFAULT_HOME_TAB;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.POSTING_APP_SIGNATURE_ENABLE_KEY;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.UPLOADING_APP_SIGNATURE_ENABLE_KEY;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ARG_IS_LOGGED_IN = "selectedRingtoneKey";

    //Preferences xml keys
    private static final String SELECTED_NOTIFICATIONS_SOUND = "pref_notifications_select_sound_key";
    private static final String POSTING_CATEGORY = "pref_category_posting_key";
    private static final String UPLOADING_CATEGORY = "pref_category_uploading_key";

    //SharedPreferences keys
    private static final int REQUEST_CODE_ALERT_RINGTONE = 2;
    public static final String SETTINGS_SHARED_PREFS = "settingsSharedPrefs";
    public static final String SELECTED_RINGTONE = "selectedRingtoneKey";
    private static final String SILENT_SELECTED = "STFU";

    private SharedPreferences settingsFile;

    private boolean isLoggedIn = false;
    private ArrayList<String> defaultHomeTabEntries = new ArrayList<>();
    private ArrayList<String> defaultHomeTabValues = new ArrayList<>();

    public static SettingsFragment newInstance(boolean isLoggedIn) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_LOGGED_IN, isLoggedIn);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        defaultHomeTabEntries.add("Recent");
        defaultHomeTabEntries.add("Forum");
        defaultHomeTabEntries.add("Unread");

        defaultHomeTabValues.add("0");
        defaultHomeTabValues.add("1");
        defaultHomeTabValues.add("2");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null)
            isLoggedIn = args.getBoolean(ARG_IS_LOGGED_IN, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_preferences);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(POSTING_CATEGORY).setVisible(isLoggedIn);
        findPreference(POSTING_APP_SIGNATURE_ENABLE_KEY).setVisible(isLoggedIn);

        //findPreference(UPLOADING_CATEGORY).setVisible(isLoggedIn);
        //findPreference(UPLOADING_APP_SIGNATURE_ENABLE_KEY).setVisible(isLoggedIn);

        if (!isLoggedIn && defaultHomeTabEntries.contains("Unread")) {
            defaultHomeTabEntries.remove("Unread");
            defaultHomeTabValues.remove("2");
        } else if (isLoggedIn && !defaultHomeTabEntries.contains("Unread")) {
            defaultHomeTabEntries.add("Unread");
            defaultHomeTabValues.add("2");
        }

        CharSequence[] tmpCs = defaultHomeTabEntries.toArray(new CharSequence[defaultHomeTabEntries.size()]);
        ((ListPreference) findPreference(DEFAULT_HOME_TAB)).setEntries(tmpCs);

        tmpCs = defaultHomeTabValues.toArray(new CharSequence[defaultHomeTabValues.size()]);
        ((ListPreference) findPreference(DEFAULT_HOME_TAB)).setEntryValues(tmpCs);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(SELECTED_NOTIFICATIONS_SOUND)) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            Activity activity = this.getActivity();
            settingsFile = activity != null
                    ? activity.getSharedPreferences(SETTINGS_SHARED_PREFS, Context.MODE_PRIVATE)
                    : null;
            String existingValue = settingsFile != null
                    ? settingsFile.getString(SELECTED_RINGTONE, null)
                    : null;
            if (existingValue != null) {
                if (existingValue.equals(SILENT_SELECTED)) {
                    //Selects "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                //No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            SharedPreferences.Editor editor = settingsFile.edit();
            if (ringtone != null) {
                editor.putString(SELECTED_RINGTONE, ringtone.toString()).apply();
            } else {
                //"Silent" was selected
                editor.putString(SELECTED_RINGTONE, SILENT_SELECTED).apply();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void updateUserLoginState(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;

        findPreference(POSTING_CATEGORY).setVisible(isLoggedIn);
        findPreference(POSTING_APP_SIGNATURE_ENABLE_KEY).setVisible(isLoggedIn);

        //findPreference(UPLOADING_CATEGORY).setVisible(isLoggedIn);
        //findPreference(UPLOADING_APP_SIGNATURE_ENABLE_KEY).setVisible(isLoggedIn);

        if (!isLoggedIn && defaultHomeTabEntries.contains("Unread")) {
            defaultHomeTabEntries.remove("Unread");
            defaultHomeTabValues.remove("2");
        } else if (isLoggedIn && !defaultHomeTabEntries.contains("Unread")) {
            defaultHomeTabEntries.add("Unread");
            defaultHomeTabValues.add("2");
        }

        CharSequence[] tmpCs = defaultHomeTabEntries.toArray(new CharSequence[defaultHomeTabEntries.size()]);
        ((ListPreference) findPreference(DEFAULT_HOME_TAB)).setEntries(tmpCs);

        tmpCs = defaultHomeTabValues.toArray(new CharSequence[defaultHomeTabValues.size()]);
        ((ListPreference) findPreference(DEFAULT_HOME_TAB)).setEntryValues(tmpCs);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean enabled;
        if (key.equals(getString(R.string.pref_privacy_crashlytics_enable_key))) {
            enabled = sharedPreferences.getBoolean(key, false);
            if(enabled)
                Timber.i("Crashlytics collection will be enabled after restarting.");
            else
                Timber.i("Crashlytics collection will be disabled after restarting.");
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "This change will take effect once you restart the app.", Toast.LENGTH_SHORT).show();
        } else if (key.equals(getString(R.string.pref_privacy_analytics_enable_key))) {
            enabled = sharedPreferences.getBoolean(key, false);
            BaseApplication.getInstance().firebaseAnalyticsCollection(enabled);
            if(enabled)
                Timber.i("Analytics collection enabled.");
            else
                Timber.i("Analytics collection disabled.");
        }
    }
}