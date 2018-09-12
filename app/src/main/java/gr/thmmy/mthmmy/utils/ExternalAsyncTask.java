package gr.thmmy.mthmmy.utils;

import android.os.AsyncTask;

public abstract class ExternalAsyncTask<U, V> extends AsyncTask<U, Void, V> {

    protected OnParseTaskStartedListener onParseTaskStartedListener;
    protected OnParseTaskCancelledListener onParseTaskCancelledListener;
    protected OnParseTaskFinishedListener<V> onParseTaskFinishedListener;

    @Override
    protected void onPreExecute() {
        if (onParseTaskStartedListener != null)
            onParseTaskStartedListener.onParseStart();
        else
            super.onPreExecute();
    }

    @Override
    protected void onCancelled() {
        if (onParseTaskCancelledListener != null)
            onParseTaskCancelledListener.onParseCancel();
        else
            super.onCancelled();
    }

    @Override
    protected void onCancelled(V v) {
        if (onParseTaskCancelledListener != null)
            onParseTaskCancelledListener.onParseCancel();
        else
            super.onCancelled();
    }

    @Override
    protected void onPostExecute(V v) {
        if (onParseTaskFinishedListener != null)
            onParseTaskFinishedListener.onParseFinish(v);
        else
            super.onPostExecute(v);
    }

    public ExternalAsyncTask(OnParseTaskStartedListener onParseTaskStartedListener, OnParseTaskCancelledListener onParseTaskCancelledListener,
                        OnParseTaskFinishedListener<V> onParseTaskFinishedListener) {
        this.onParseTaskStartedListener = onParseTaskStartedListener;
        this.onParseTaskCancelledListener = onParseTaskCancelledListener;
        this.onParseTaskFinishedListener = onParseTaskFinishedListener;
    }

    public ExternalAsyncTask(OnParseTaskStartedListener onParseTaskStartedListener, OnParseTaskFinishedListener<V> onParseTaskFinishedListener) {
        this.onParseTaskStartedListener = onParseTaskStartedListener;
        this.onParseTaskFinishedListener = onParseTaskFinishedListener;
    }

    public ExternalAsyncTask() { }

    public void setOnParseTaskStartedListener(OnParseTaskStartedListener onParseTaskStartedListener) {
        this.onParseTaskStartedListener = onParseTaskStartedListener;
    }

    public void setOnParseTaskCancelledListener(OnParseTaskCancelledListener onParseTaskCancelledListener) {
        this.onParseTaskCancelledListener = onParseTaskCancelledListener;
    }

    public void setOnParseTaskFinishedListener(OnParseTaskFinishedListener<V> onParseTaskFinishedListener) {
        this.onParseTaskFinishedListener = onParseTaskFinishedListener;
    }

    public interface OnParseTaskStartedListener {
        void onParseStart();
    }

    public interface OnParseTaskCancelledListener {
        void onParseCancel();
    }

    public interface OnParseTaskFinishedListener<V> {
        void onParseFinish(V result);
    }
}
