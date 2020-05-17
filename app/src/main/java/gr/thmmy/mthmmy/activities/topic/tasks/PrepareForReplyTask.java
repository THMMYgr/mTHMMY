package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Selector;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class PrepareForReplyTask extends AsyncTask<Integer, Void, PrepareForReplyResult> {
    private PrepareForReplyCallbacks listener;
    private OnPrepareForReplyFinished finishListener;
    private String replyPageUrl;

    public PrepareForReplyTask(PrepareForReplyCallbacks listener, OnPrepareForReplyFinished finishListener,
                               String replyPageUrl) {
        this.listener = listener;
        this.finishListener = finishListener;
        this.replyPageUrl = replyPageUrl;
    }

    @Override
    protected void onPreExecute() {
        listener.onPrepareForReplyStarted();
    }

    @Override
    protected PrepareForReplyResult doInBackground(Integer... postIndices) {
        Document document;
        Request request = new Request.Builder()
                .url(replyPageUrl + ";wap2")
                .build();

        OkHttpClient client = BaseApplication.getInstance().getClient();
        String numReplies, seqnum, sc, topic;
        try {
            Response response = client.newCall(request).execute();
            document = Jsoup.parse(response.body().string());

            numReplies = replyPageUrl.substring(replyPageUrl.indexOf("num_replies=") + 12);
            seqnum = document.select("input[name=seqnum]").first().attr("value");
            sc = document.select("input[name=sc]").first().attr("value");
            topic = document.select("input[name=topic]").first().attr("value");
        } catch (NullPointerException e) {
            // TODO: Convert this task to (New)ParseTask (?) / handle parsing errors in a better way
            Timber.e(e, "Prepare failed (1)");
            return new PrepareForReplyResult(false, null, null, null, null, null);
        } catch (IOException | Selector.SelectorParseException e){
            Timber.e(e, "Prepare failed (2)");
            return new PrepareForReplyResult(false, null, null, null, null, null);
        }

        StringBuilder buildedQuotes = new StringBuilder();
        for (Integer postIndex : postIndices) {
            request = new Request.Builder()
                    .url("https://www.thmmy.gr/smf/index.php?action=quotefast;quote=" +
                           postIndex + ";" + "sesc=" + sc + ";xml")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                body = Parser.unescapeEntities(body, false);
                buildedQuotes.append(body.substring(body.indexOf("<quote>") + 7, body.indexOf("</quote>")));
                buildedQuotes.append("\n\n");
            } catch (IOException | Selector.SelectorParseException e) {
                Timber.e(e, "Quote building failed.");
                return new PrepareForReplyResult(false, null, null, null, null, null);
            }
        }
        return new PrepareForReplyResult(true, numReplies, seqnum, sc, topic, buildedQuotes.toString());
    }

    @Override
    protected void onPostExecute(PrepareForReplyResult result) {
        finishListener.onPrepareForReplyFinished(result);
    }

    public interface PrepareForReplyCallbacks {
        void onPrepareForReplyStarted();
        void onPrepareForReplyCancelled();
    }

    public interface OnPrepareForReplyFinished {
        void onPrepareForReplyFinished(PrepareForReplyResult result);
    }
}
