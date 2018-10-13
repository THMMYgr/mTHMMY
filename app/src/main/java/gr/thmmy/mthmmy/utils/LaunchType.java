package gr.thmmy.mthmmy.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.base.BaseApplication;

public class LaunchType {
    public enum LAUNCH_TYPE {
        FIRST_LAUNCH_EVER, FIRST_LAUNCH_AFTER_UPDATE, NORMAL_LAUNCH, INDETERMINATE
    }

    private static final String PREF_VERSION_CODE_KEY = "VERSION_CODE";

    public static LAUNCH_TYPE getLaunchType() {
        final int notThere = -1;
        //Gets current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        //Gets saved version code
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance().getContext());
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, notThere);
        //Checks for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            //This is just a normal run
            return LAUNCH_TYPE.NORMAL_LAUNCH;
        } else if (savedVersionCode == notThere) {
            //Updates the shared preferences with the current version code
            prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
            return LAUNCH_TYPE.FIRST_LAUNCH_EVER;
        } else if (currentVersionCode > savedVersionCode) {
            //Updates the shared preferences with the current version code
            prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
            return LAUNCH_TYPE.FIRST_LAUNCH_AFTER_UPDATE;
        }
        //Probably shared preferences were manually changed by the user
        return LAUNCH_TYPE.INDETERMINATE;
    }
}

