package gr.thmmy.mthmmy.utils.parsing;

import org.jsoup.nodes.Document;

import gr.thmmy.mthmmy.utils.NetworkTask;
import okhttp3.Response;

public abstract class NewParseTask<T> extends NetworkTask<T> {

    public NewParseTask(OnTaskStartedListener onTaskStartedListener, OnTaskCancelledListener onTaskCancelledListener,
                        OnNetworkTaskFinishedListener<T> onParseTaskFinishedListener) {
        super(onTaskStartedListener, onTaskCancelledListener, onParseTaskFinishedListener);
    }

    public NewParseTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<T> onParseTaskFinishedListener) {
        super(onTaskStartedListener, onParseTaskFinishedListener);
    }

    public NewParseTask() {}

    @Override
    protected final T performTask(Document document, Response response) {
        try {
            return parse(document, response);
        } catch (Exception e) {
            throw new ParseException("Parse failed.", e);
        }
    }

    protected abstract T parse (Document document, Response response) throws ParseException;
}
