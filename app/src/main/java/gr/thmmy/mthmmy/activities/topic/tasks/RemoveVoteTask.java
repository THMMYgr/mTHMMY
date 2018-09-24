package gr.thmmy.mthmmy.activities.topic.tasks;

import org.jsoup.nodes.Document;

import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.NetworkTask;
import okhttp3.Response;

public class RemoveVoteTask extends NetworkTask<Void> {

    @Override
    protected Void performTask(Document document, Response response) {
        return null;
    }

    @Override
    protected int getResultCode(Response response, Void data) {
        return NetworkResultCodes.SUCCESSFUL;
    }
}
