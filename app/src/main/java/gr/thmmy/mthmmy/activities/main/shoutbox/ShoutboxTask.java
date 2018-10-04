package gr.thmmy.mthmmy.activities.main.shoutbox;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import okhttp3.Response;

public class ShoutboxTask extends NewParseTask<ArrayList<Shout>> {
    @Override
    protected ArrayList<Shout> parse(Document document, Response response) throws ParseException {
        // shout container: document.select("div[class=smalltext]" && div.text().contains("Τελευταίες 75 φωνές:") η στα αγγλικα
        Element shoutboxContainer = document.select("div[style=width: 99%; height: 600px; overflow: auto;]").first();
        ArrayList<Shout> shouts = new ArrayList<>();
        for (Element shout : shoutboxContainer.children()) {
            Element user = shout.child(0);
            Element link = user.select("a").first();
            String profileUrl = link.attr("href");
            String profileName = link.text();

            Element date = shout.child(1);
            String dateString = date.text();

            Element content = shout.child(2);
            String shoutContent = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" +
                    ParseHelpers.youtubeEmbeddedFix(content);
            shouts.add(new Shout(profileName, profileUrl, dateString, shoutContent));
        }
        return shouts;
    }

    @Override
    protected int getResultCode(Response response, ArrayList<Shout> data) {
        return data.size() > 0 ? NetworkResultCodes.SUCCESSFUL : NetworkResultCodes.PERFORM_TASK_ERROR;
    }
}
