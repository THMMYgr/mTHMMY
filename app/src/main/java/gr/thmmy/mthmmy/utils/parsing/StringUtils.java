package gr.thmmy.mthmmy.utils.parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * Method that extracts the base URL from a topic's page URL. For example a topic with url similar to
     * "https://www.thmmy.gr/smf/index.php?topic=1.15;topicseen" or
     * "https://www.thmmy.gr/smf/index.php?topic=1.msg1#msg1"
     * has the base url "https://www.thmmy.gr/smf/index.php?topic=1"
     *
     * @param topicURL a topic's page URL
     * @return the base URL of the given topic
     */
    public static String getBaseURL(String topicURL) {
        String forumUrl = "https://www.thmmy.gr/smf/index.php?";
        Matcher baseUrlMatcher = Pattern.compile("topic=[0-9]+").matcher(topicURL);
        if (baseUrlMatcher.find())
            return forumUrl + topicURL.substring(baseUrlMatcher.start(), baseUrlMatcher.end());
        else return "";
    }

    public static int extractUserCodeFromUrl(String url) {
        Matcher userCodeMatcher = Pattern.compile("u=[0-9]+").matcher(url);
        if (userCodeMatcher.find())
            return Integer.parseInt(url.substring(userCodeMatcher.start()+2, userCodeMatcher.end()));
        else return -1;
    }
}
