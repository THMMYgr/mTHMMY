package gr.thmmy.mthmmy.session;

import android.os.AsyncTask;

import gr.thmmy.mthmmy.base.BaseApplication;


public class ValidateSessionTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        BaseApplication.getInstance().getSessionManager().validateSession();
        return null;
    }

    public boolean isRunning(){
        return getStatus() == AsyncTask.Status.RUNNING;
    }
}
