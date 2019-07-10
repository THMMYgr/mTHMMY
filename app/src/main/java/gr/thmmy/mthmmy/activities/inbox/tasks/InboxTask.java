package gr.thmmy.mthmmy.activities.inbox.tasks;

import org.jsoup.nodes.Document;

import gr.thmmy.mthmmy.model.Inbox;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import okhttp3.Response;

public class InboxTask extends NewParseTask<Inbox> {
    @Override
    protected Inbox parse(Document document, Response response) throws ParseException {
        return null;
    }

    @Override
    protected int getResultCode(Response response, Inbox data) {
        return 0;
    }
}


