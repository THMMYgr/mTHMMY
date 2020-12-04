package gr.thmmy.mthmmy.utils;

import android.os.AsyncTask;

public abstract class ExternalAsyncTask<U, V> extends AsyncTask<U, Void, V> {

    protected OnTaskStartedListener onTaskStartedListener;
    protected OnTaskCancelledListener onTaskCancelledListener;
    protected OnTaskFinishedListener<V> onTaskFinishedListener;

    @Override
    protected void onPreExecute() {
        if (onTaskStartedListener != null)
            onTaskStartedListener.onTaskStarted();
        else
            super.onPreExecute();
    }

    @Override
    protected void onCancelled() {
        if (onTaskCancelledListener != null)
            onTaskCancelledListener.onTaskCanceled();
        else
            super.onCancelled();
    }

    @Override
    protected void onCancelled(V v) {
        if (onTaskCancelledListener != null)
            onTaskCancelledListener.onTaskCanceled();
        else
            super.onCancelled();
    }

    @Override
    protected void onPostExecute(V v) {
        if (onTaskFinishedListener != null)
            onTaskFinishedListener.onTaskFinished(v);
        else
            super.onPostExecute(v);
    }

    public ExternalAsyncTask(OnTaskStartedListener onTaskStartedListener, OnTaskCancelledListener onTaskCancelledListener,
                             OnTaskFinishedListener<V> onTaskFinishedListener) {
        this.onTaskStartedListener = onTaskStartedListener;
        this.onTaskCancelledListener = onTaskCancelledListener;
        this.onTaskFinishedListener = onTaskFinishedListener;
    }

    public ExternalAsyncTask(OnTaskStartedListener onTaskStartedListener, OnTaskFinishedListener<V> onTaskFinishedListener) {
        this.onTaskStartedListener = onTaskStartedListener;
        this.onTaskFinishedListener = onTaskFinishedListener;
    }

    public ExternalAsyncTask() {
    }

    public void setOnTaskStartedListener(OnTaskStartedListener onTaskStartedListener) {
        this.onTaskStartedListener = onTaskStartedListener;
    }

    public void setOnTaskCancelledListener(OnTaskCancelledListener onTaskCancelledListener) {
        this.onTaskCancelledListener = onTaskCancelledListener;
    }

    public void setOnTaskFinishedListener(OnTaskFinishedListener<V> onTaskFinishedListener) {
        this.onTaskFinishedListener = onTaskFinishedListener;
    }

    public interface OnTaskStartedListener {
        void onTaskStarted();
    }

    public interface OnTaskCancelledListener {
        void onTaskCanceled();
    }

    public interface OnTaskFinishedListener<V> {
        void onTaskFinished(V result);
    }

    public boolean isRunning() {
        return getStatus() == AsyncTask.Status.RUNNING;
    }
}
