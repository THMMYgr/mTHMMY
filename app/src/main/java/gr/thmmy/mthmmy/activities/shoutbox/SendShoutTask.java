package gr.thmmy.mthmmy.activities.shoutbox;

import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.networking.NetworkTask;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SendShoutTask extends NetworkTask<Void> {

    public SendShoutTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<Void> onNetworkTaskFinishedListener) {
        super(onTaskStartedListener, onNetworkTaskFinishedListener);
    }

    @Override
    protected Response sendRequest(OkHttpClient client, String... input) throws IOException {
        MultipartBody.Builder postBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("sc", input[2])
                .addFormDataPart("tp-shout", input[1])
                .addFormDataPart("tp-shout-name", input[3])
                .addFormDataPart("shout_send", input[4])
                .addFormDataPart("tp-shout-url", input[5]);

        Request voteRequest = new Request.Builder()
                .url(input[0])
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .post(postBodyBuilder.build())
                .build();
        client.newCall(voteRequest).execute();
        return client.newCall(voteRequest).execute();
    }



    @Override
    protected Void performTask(Document document, Response response) {
        return null;
    }

    @Override
    protected int getResultCode(Response response, Void data) {
        return NetworkResultCodes.SUCCESSFUL;
    }
}
