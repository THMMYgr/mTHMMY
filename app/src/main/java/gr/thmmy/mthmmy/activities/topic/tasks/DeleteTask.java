package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;

import java.io.IOException;

import gr.thmmy.mthmmy.activities.topic.Posting;
import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class DeleteTask extends AsyncTask<String, Void, Boolean> {
    private DeleteTaskCallbacks listener;

    public DeleteTask(DeleteTaskCallbacks listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onDeleteTaskStarted();
    }

    @Override
    protected Boolean doInBackground(String... args) {
        Request delete = new Request.Builder()
                .url(args[0])
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .build();

        try {
            OkHttpClient client = BaseApplication.getInstance().getClient();
            client.newCall(delete).execute();
            Response response = client.newCall(delete).execute();
            //Response response = client.newCall(delete).execute();
            switch (Posting.replyStatus(response)) {
                case SUCCESSFUL:
                    BaseApplication.getInstance().logFirebaseAnalyticsEvent("post_deletion", null);
                    return true;
                default:
                    Timber.e("Something went wrong. Request string: %s", delete.toString());
                    return false;
            }
        } catch (IOException e) {
            Timber.e(e, "Delete failed.");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        listener.onDeleteTaskFinished(result);
    }

    public interface DeleteTaskCallbacks {
        void onDeleteTaskStarted();
        void onDeleteTaskFinished(boolean result);
    }
}
