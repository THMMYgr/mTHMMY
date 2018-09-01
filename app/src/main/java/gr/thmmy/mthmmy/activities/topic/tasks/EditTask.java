package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.topic.Posting.replyStatus;

public class EditTask extends AsyncTask<String, Void, Boolean> {
    private EditTaskCallbacks listener;
    private int position;

    public EditTask(EditTaskCallbacks listener, int position) {
        this.listener = listener;
        this.position = position;
    }

    @Override
    protected void onPreExecute() {
        listener.onEditTaskStarted();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", strings[1])
                .addFormDataPart("num_replies", strings[2])
                .addFormDataPart("seqnum", strings[3])
                .addFormDataPart("sc", strings[4])
                .addFormDataPart("subject", strings[5])
                .addFormDataPart("topic", strings[6])
                .build();
        Request post = new Request.Builder()
                .url(strings[0])
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .post(postBody)
                .build();

        try {
            OkHttpClient client = BaseApplication.getInstance().getClient();
            client.newCall(post).execute();
            Response response = client.newCall(post).execute();
            switch (replyStatus(response)) {
                case SUCCESSFUL:
                    BaseApplication.getInstance().logFirebaseAnalyticsEvent("post_editing", null);
                    return true;
                case NEW_REPLY_WHILE_POSTING:
                    //TODO this...
                    return true;
                default:
                    Timber.e("Malformed post. Request string: %s", post.toString());
                    return true;
            }
        } catch (IOException e) {
            Timber.e(e, "Edit failed.");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        listener.onEditTaskFinished(result, position);
    }

    public interface EditTaskCallbacks {
        void onEditTaskStarted();
        void onEditTaskFinished(boolean result, int position);
    }
}
