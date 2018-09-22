package gr.thmmy.mthmmy.utils.parsing;

import org.jsoup.nodes.Document;

import gr.thmmy.mthmmy.utils.NetworkTask;

public abstract class NewParseTask<T> extends NetworkTask<T> {

    public NewParseTask(OnParseTaskStartedListener onParseTaskStartedListener, OnParseTaskCancelledListener onParseTaskCancelledListener,
                        OnParseTaskFinishedListener<T> onParseTaskFinishedListener) {
        super(onParseTaskStartedListener, onParseTaskCancelledListener, onParseTaskFinishedListener);
    }

    public NewParseTask(OnParseTaskStartedListener onParseTaskStartedListener, OnParseTaskFinishedListener<T> onParseTaskFinishedListener) {
        super(onParseTaskStartedListener, onParseTaskFinishedListener);
    }

    public NewParseTask() {}

    @Override
    protected final T performTask(Document document) {
        try {
            return parse(document);
        } catch (Exception e) {
            throw new ParseException("Parse failed.", e);
        }
    }

    protected abstract T parse (Document document) throws ParseException;
}
