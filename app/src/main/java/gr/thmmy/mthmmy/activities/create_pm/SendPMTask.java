package gr.thmmy.mthmmy.activities.create_pm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.ExternalAsyncTask;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.topic.Posting.replyStatus;

public class SendPMTask extends ExternalAsyncTask<String, Boolean> {

    private boolean includeAppSignature;

    public SendPMTask(boolean includeAppSignature) {
        this.includeAppSignature = includeAppSignature;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Request request = new Request.Builder()
                .url(strings[0] + ";wap2")
                .build();

        OkHttpClient client = BaseApplication.getInstance().getClient();

        Document document;
        String seqnum, sc, outbox, createTopicUrl, replied_to, folder;
        try {
            Response response = client.newCall(request).execute();
            document = Jsoup.parse(response.body().string());

            seqnum = document.select("input[name=seqnum]").first().attr("value");
            sc = document.select("input[name=sc]").first().attr("value");
            outbox = document.select("input[name=outbox]").first().attr("value");
            replied_to = document.select("input[name=replied_to]").first().attr("value");
            folder = document.select("input[name=folder]").first().attr("value");
            createTopicUrl = document.select("form").first().attr("action");

            final String appSignature = "\n[right][size=7pt][i]sent from [url=https://play.google.com/store/apps/" +
                    "details?id=gr.thmmy.mthmmy]mTHMMY[/url]  [/i][/size][/right]";

            RequestBody postBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("message", strings[3] + (includeAppSignature ? appSignature : ""))
                    .addFormDataPart("seqnum", seqnum)
                    .addFormDataPart("sc", sc)
                    .addFormDataPart("u", strings[1]) // recipient
                    .addFormDataPart("subject", strings[2])
                    .addFormDataPart("outbox", outbox)
                    .addFormDataPart("replied_to", replied_to)
                    .addFormDataPart("folder", folder)
                    .build();

            Request pmRequest = new Request.Builder()
                    .url(createTopicUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .post(postBody)
                    .build();

            try {
                client.newCall(pmRequest).execute();
                Response response2 = client.newCall(pmRequest).execute();
                switch (replyStatus(response2)) {
                    case SUCCESSFUL:
                        BaseApplication.getInstance().logFirebaseAnalyticsEvent("new_topic_creation", null);
                        return true;
                    default:
                        Timber.e("Malformed pmRequest. Request string: %s", pmRequest.toString());
                        return false;
                }
            } catch (IOException e) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }
}
