package gr.thmmy.mthmmy.activities.create_topic;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.topic.Posting.replyStatus;

public class NewTopicTask extends AsyncTask<String, Void, Boolean> {

    private NewTopicTaskCallbacks listener;
    private boolean includeAppSignature;

    public NewTopicTask(NewTopicTaskCallbacks listener, boolean includeAppSignature){
        this.listener = listener;
        this.includeAppSignature = includeAppSignature;
    }

    @Override
    protected void onPreExecute() {
        listener.onNewTopicTaskStarted();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Request request = new Request.Builder()
                .url(strings[0] + ";wap2")
                .build();

        OkHttpClient client = BaseApplication.getInstance().getClient();

        Document document;
        String seqnum, sc, topic, createTopicUrl;
        try {
            Response response = client.newCall(request).execute();
            document = Jsoup.parse(response.body().string());

            seqnum = document.select("input[name=seqnum]").first().attr("value");
            sc = document.select("input[name=sc]").first().attr("value");
            topic = document.select("input[name=topic]").first().attr("value");
            createTopicUrl = document.select("form").first().attr("action");

            final String appSignature = "\n[right][size=7pt][i]sent from [url=https://play.google.com/store/apps/" +
                    "details?id=gr.thmmy.mthmmy]mTHMMY[/url]  [/i][/size][/right]";

            RequestBody postBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("message", strings[2] + (includeAppSignature ? appSignature : ""))
                    .addFormDataPart("seqnum", seqnum)
                    .addFormDataPart("sc", sc)
                    .addFormDataPart("subject", strings[1])
                    .addFormDataPart("topic", topic)
                    .build();

            Request post = new Request.Builder()
                    .url(createTopicUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .post(postBody)
                    .build();

            try {
                client.newCall(post).execute();
                Response response2 = client.newCall(post).execute();
                switch (replyStatus(response2)) {
                    case SUCCESSFUL:
                        BaseApplication.getInstance().logFirebaseAnalyticsEvent("new_topic_creation", null);
                        return true;
                    default:
                        Timber.e("Malformed post. Request string: %s", post.toString());
                        return false;
                }
            } catch (IOException e) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.onNewTopicTaskFinished(success);
    }

    public interface NewTopicTaskCallbacks {
        void onNewTopicTaskStarted();
        void onNewTopicTaskFinished(boolean success);
    }
}
