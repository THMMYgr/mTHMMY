package gr.thmmy.mthmmy.activities.topic.tasks;

import org.jsoup.nodes.Document;

import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.networking.NetworkTask;
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
