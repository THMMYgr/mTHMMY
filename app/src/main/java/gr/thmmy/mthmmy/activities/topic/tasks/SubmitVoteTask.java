package gr.thmmy.mthmmy.activities.topic.tasks;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;

import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.NetworkTask;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class SubmitVoteTask extends NetworkTask<Void> {

    private int[] votes;

    public SubmitVoteTask(int... votes) {
        this.votes = votes;
    }

    @Override
    protected Response sendRequest(OkHttpClient client, String... input) throws IOException {
        MultipartBody.Builder postBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("sc", input[1]);
        for (int vote : votes) {
            postBodyBuilder.addFormDataPart("options[]", Integer.toString(vote));
        }
        Timber.d("response" + Arrays.toString(votes));

        Request voteRequest = new Request.Builder()
                .url(input[0])
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                .post(postBodyBuilder.build())
                .build();
        return client.newCall(voteRequest).execute();
    }

    @Override
    protected Void performTask(Document document, Response response) {
        return null;
    }

    @Override
    protected int getResultCode(Response response, Void data) {
        Timber.d("response" + response);
        return NetworkResultCodes.SUCCESSFUL;
    }
}
