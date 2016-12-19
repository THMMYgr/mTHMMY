package gr.thmmy.mthmmy.activities.profile;

import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import mthmmy.utils.Report;

class ProfileParser {
    //Parsing variables
    private static String nameSelect;
    private static String signatureSelect;

    //Other variables
    @SuppressWarnings("unused")
    private static final String TAG = "ProfileParser";
    static final int THUMBNAIL_URL = 0;
    static final int NAME_INDEX = 1;
    static final int PERSONAL_TEXT_INDEX = 2;

    static ArrayList<String> parseProfile(Document doc) {
        defineLanguage(doc);

        //Method's variables
        ArrayList<String> returnArray = new ArrayList<>();

        //Contains all summary's rows
        Elements summaryRows = doc.select("td.windowbg:nth-child(1)");

        { //Find thumbnail url
            Element tmpEl = doc.select(".bordercolor img.avatar").first();
            if (tmpEl != null)
                returnArray.add(THUMBNAIL_URL, tmpEl.attr("abs:src"));
            else //User doesn't have an avatar
                returnArray.add(THUMBNAIL_URL, null);
        }

        { //Find username
            Element tmpEl = summaryRows.select("tr:contains(" + nameSelect + ")").first();
            if (tmpEl != null) {
                returnArray.add(NAME_INDEX, tmpEl.select("td").get(1).text());
            } else {
                //Should never get here!
                //Something is wrong.
                Report.e(TAG, "An error occurred while trying to find profile's username.");
            }
        }

        { //Find personal text
            String tmpPersonalText = doc.select("td.windowbg:nth-child(2)").first().text().trim();
            returnArray.add(PERSONAL_TEXT_INDEX, tmpPersonalText);
        }

        for (Element row : summaryRows.select("tr")) {
            String rowText = row.text(), tmpHtml = "";

            if (row.select("td").size() == 1)
                tmpHtml = "";
            else if (rowText.contains(signatureSelect)) {
                tmpHtml = row.html();
            } else if (!rowText.contains(nameSelect)) {
                if (Objects.equals(row.select("td").get(1).text(), ""))
                    continue;
                tmpHtml = "<b>" + row.select("td").first().text() + "</b> "
                        + row.select("td").get(1).text();
            }
            returnArray.add(tmpHtml);
        }
        return returnArray;
    }

    private static void defineLanguage(Document doc) {
        //English parsing variables
        final String en_nameSelect = "Name";
        final String en_signatureSelect = "Signature";

        //Greek parsing variables
        final String gr_nameSelect = "Όνομα";
        final String gr_signatureSelect = "Υπογραφή";

        if (doc.select("h3").text().contains("Καλώς ορίσατε")) {
            nameSelect = gr_nameSelect;
            signatureSelect = gr_signatureSelect;

        } else { //Default is english (eg. guest's language)
            nameSelect = en_nameSelect;
            signatureSelect = en_signatureSelect;
        }
    }
}
