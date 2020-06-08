package gr.thmmy.mthmmy.utils.crashreporting;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import timber.log.Timber.DebugTree;

public class CrashReportingTree extends DebugTree {
    private  FirebaseCrashlytics firebaseCrashlytics;
    public CrashReportingTree() {
        super();
        firebaseCrashlytics = FirebaseCrashlytics.getInstance();
    }

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

        firebaseCrashlytics.log(level + "/" + tag + ": " + message);

        if(priority == Log.ERROR) {
            if (t!=null)
                firebaseCrashlytics.recordException(t);
            else
                firebaseCrashlytics.recordException(new Exception(message));
        }
    }
}
