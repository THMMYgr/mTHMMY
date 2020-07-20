package gr.thmmy.mthmmy.activities.topic.tasks;

import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.networking.NetworkTask;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeleteTask extends NetworkTask<Void> {

    public DeleteTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<Void> onParseTaskFinishedListener) {
        super(onTaskStartedListener, onParseTaskFinishedListener);
    }

    @Override
    protected Response sendRequest(OkHttpClient client, String... input) throws IOException {
        Request delete = new Request.Builder()
                .url(input[0])
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .build();
        client.newCall(delete).execute();
        return client.newCall(delete).execute();
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
