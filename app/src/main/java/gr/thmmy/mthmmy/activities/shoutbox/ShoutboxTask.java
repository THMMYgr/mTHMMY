package gr.thmmy.mthmmy.activities.shoutbox;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.model.Shoutbox;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import okhttp3.Response;

public class ShoutboxTask extends NewParseTask<Shoutbox> {

    public ShoutboxTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<Shoutbox> onParseTaskFinishedListener) {
        super(onTaskStartedListener, onParseTaskFinishedListener);
    }

    @Override
    protected Shoutbox parse(Document document, Response response) throws ParseException {
        // fragment_shoutbox_shout_row container: document.select("div[class=smalltext]" && div.text().contains("Τελευταίες 75 φωνές:") η στα αγγλικα
        Element shoutboxContainer = document.select("table.windowbg").first();
        ArrayList<Shout> shouts = new ArrayList<>();
        for (Element shout : shoutboxContainer.select("div[style=margin: 4px;]")) {
            Element user = shout.child(0);
            Element link = user.select("a").first();
            String profileUrl = link.attr("href");
            String profileName = link.text();
            boolean memberOfTheMonth = link.attr("style").contains("#EA00FF");

            Element date = shout.child(1);
            String dateString = date.text();

            Element content = shout.child(2);
            content.removeAttr("style");
            String shoutContent = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" +
                    ParseHelpers.youtubeEmbeddedFix(content);
            shouts.add(new Shout(profileName, profileUrl, dateString, shoutContent, memberOfTheMonth));
        }

        Element shoutboxForm = document.select("form[name=tp-shoutbox]").first();
        String formUrl = shoutboxForm.attr("action");
        String sc = shoutboxForm.select("input[name=sc]").first().attr("value");
        String shoutName = shoutboxForm.select("input[name=tp-shout-name]").first().attr("value");

        Element shoutSendInput = shoutboxForm.select("input[name=shout_send]").first();
        String shoutSend = null;
        if (shoutSendInput != null)
            shoutSend = shoutSendInput.attr("value");
        String shoutUrl = shoutboxForm.select("input[name=tp-shout-url]").first().attr("value");
        return new Shoutbox(shouts.toArray(new Shout[0]), sc, formUrl, shoutName, shoutSend, shoutUrl);
    }

    @Override
    protected int getResultCode(Response response, Shoutbox data) {
        return NetworkResultCodes.SUCCESSFUL;
    }
}
