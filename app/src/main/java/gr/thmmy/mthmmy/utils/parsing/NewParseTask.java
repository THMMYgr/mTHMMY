package gr.thmmy.mthmmy.utils.parsing;

import org.jsoup.nodes.Document;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class NewParseTask<T> extends NetworkTask<T> {

    @Override
    final T performTask(Document document) throws ParseException {
        try {
            return parse(document);
        } catch (Exception e) {
            throw new ParseException("Parse failed.", e);
        }
    }

    abstract Request createRequest(String... input);

    abstract Response getResponse(Request request, OkHttpClient client);

    abstract T parse (Document document);

    abstract int getResultCode(Response response, T data);
}
