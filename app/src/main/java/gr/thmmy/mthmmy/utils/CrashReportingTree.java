package gr.thmmy.mthmmy.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber.DebugTree;

public class CrashReportingTree extends DebugTree {
    
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        char level;

        if (priority == Log.INFO)
            level = 'I';
        else if (priority == Log.WARN)
            level = 'W';
        else if(priority == Log.ERROR)
            level = 'E';
        else
            level = 'A';

        Crashlytics.log(level + "/" + tag + ": " + message);

        if(priority == Log.ERROR)
        {
            if (t!=null)
                Crashlytics.logException(t);
            else
                Crashlytics.logException(new Exception(message));
        }

    }
}
