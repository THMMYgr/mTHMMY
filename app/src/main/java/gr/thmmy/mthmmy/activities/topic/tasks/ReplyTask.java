package gr.thmmy.mthmmy.activities.topic.tasks;

import static gr.thmmy.mthmmy.activities.topic.Posting.replyStatus;

import android.os.AsyncTask;

import java.io.IOException;

import gr.thmmy.mthmmy.activities.topic.Posting;
import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class ReplyTask extends AsyncTask<String, Void, Posting.REPLY_STATUS> {
    private ReplyTaskCallbacks listener;
    private boolean includeAppSignature;

    public ReplyTask(ReplyTaskCallbacks listener, boolean includeAppSignature) {
        this.listener = listener;
        this.includeAppSignature = includeAppSignature;
    }

    @Override
    protected void onPreExecute() {
        listener.onReplyTaskStarted();
    }

    @Override
    protected Posting.REPLY_STATUS doInBackground(String... args) {
        final String sentFrommTHMMY = includeAppSignature
                ? "\n[right][size=7pt][i]sent from [url=https://play.google.com/store/apps/details?id=gr.thmmy.mthmmy]mTHMMY[/url]  [/i][/size][/right]"
                : "";
        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", args[1] + sentFrommTHMMY)
                .addFormDataPart("num_replies", args[2])
                .addFormDataPart("seqnum", args[3])
                .addFormDataPart("sc", args[4])
                .addFormDataPart("subject", args[0])
                .addFormDataPart("topic", args[5])
                .addFormDataPart("icon", "xx")
                .build();
        Request post = new Request.Builder()
                .url(BaseApplication.getForumUrl() + "index.php?action=post2")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .post(postBody)
                .build();

        try {
            OkHttpClient client = BaseApplication.getInstance().getClient();
            client.newCall(post).execute();
            Response response = client.newCall(post).execute();
            return replyStatus(response);
        } catch (IOException e) {
            Timber.e(e, "Post failed.");
            return Posting.REPLY_STATUS.OTHER_ERROR;
        }
    }

    @Override
    protected void onPostExecute(Posting.REPLY_STATUS result) {
        listener.onReplyTaskFinished(result);
    }

    public interface ReplyTaskCallbacks {
        void onReplyTaskStarted();

        void onReplyTaskFinished(Posting.REPLY_STATUS result);
    }
}
