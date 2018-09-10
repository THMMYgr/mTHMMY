package gr.thmmy.mthmmy.utils.parsing;

import android.os.AsyncTask;

public abstract class NewParseTask<U, V> extends AsyncTask<U, Void, V> {

    private OnParseTaskStartedListener onParseTaskStartedListener;
    private OnParseTaskCancelledListener onParseTaskCancelledListener;
    private OnParseTaskFinishedListener onParseTaskFinishedListener;

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
            onParseTaskFinishedListener.onParseFinish();
        else
            super.onPostExecute(v);
    }

    public void setOnParseTaskStartedListener(OnParseTaskStartedListener onParseTaskStartedListener) {
        this.onParseTaskStartedListener = onParseTaskStartedListener;
    }

    public void setOnParseTaskCancelledListener(OnParseTaskCancelledListener onParseTaskCancelledListener) {
        this.onParseTaskCancelledListener = onParseTaskCancelledListener;
    }

    public void setOnParseTaskFinishedListener(OnParseTaskFinishedListener onParseTaskFinishedListener) {
        this.onParseTaskFinishedListener = onParseTaskFinishedListener;
    }

    public interface OnParseTaskStartedListener {
        void onParseStart();
    }

    public interface OnParseTaskCancelledListener {
        void onParseCancel();
    }

    public interface OnParseTaskFinishedListener {
        void onParseFinish();
    }
}
