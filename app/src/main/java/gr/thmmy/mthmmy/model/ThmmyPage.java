package gr.thmmy.mthmmy.model;

import android.net.Uri;
import android.util.Log;

import java.util.Objects;

import timber.log.Timber;

/**
 * This class consists exclusively of static classes (enums) and methods (excluding methods of inner
 * classes). It can be used to resolve link targets as to whether they are pointing to the forum and
 * where in the forum they may point.
 */
public class ThmmyPage {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "LinkTarget";

    /**
     * An enum describing a link's target by defining the types:<ul>
     * <li>{@link #NOT_THMMY}</li>
     * <li>{@link #THMMY}</li>
     * <li>{@link #INDEX}</li>
     * <li>{@link #UNKNOWN_THMMY}</li>
     * <li>{@link #TOPIC}</li>
     * <li>{@link #BOARD}</li>
     * <li>{@link #UNREAD_POSTS}</li>
     * <li>{@link #PROFILE_SUMMARY}</li>
     * <li>{@link #PROFILE_LATEST_POSTS}</li>
     * <li>{@link #PROFILE_STATS}</li>
     * <li>{@link #PROFILE}</li>
     * </ul>
     */
    public enum PageCategory {
        /**
         * Link doesn't point to thmmy.
         */
        NOT_THMMY,
        /**
         * Link points to thmmy.
         */
        THMMY,
        /**
         * Link points to thmmy index page/
         */
        INDEX,
        /**
         * Link points to a thmmy page that's not (yet) supported by the app.
         */
        UNKNOWN_THMMY,
        /**
         * Link points to a topic.
         */
        TOPIC,
        /**
         * Link points to a board.
         */
        BOARD,
        /**
         * Link points to user's unread posts.
         */
        UNREAD_POSTS,
        /**
         * Link points to a profile's summary.
         */
        PROFILE_SUMMARY,
        /**
         * Link points to a profile's latest posts.
         */
        PROFILE_LATEST_POSTS,
        /**
         * Link points to a profile's stats.
         */
        PROFILE_STATS,
        /**
         * Link points to a profile.
         */
        PROFILE,
        /**
         * Link points to a download.
         */
        DOWNLOADS_CATEGORY,
        /**
         * Link points to a download category.
         */
        DOWNLOADS_FILE,
        /**
         * Link points to downloads.
         */
        DOWNLOADS;

        /**
         * This method defines a custom equality check for {@link PageCategory} enums. It does not check
         * whether a url is equal to another.
         * <p>Method returns true if parameter's Target is the same as the object and in the specific
         * cases described below, false otherwise.</p><ul>
         * <li>(Everything but {@link #NOT_THMMY}).is({@link #THMMY}) returns true</li>
         * <li>{@link #PROFILE_SUMMARY}.is({@link #PROFILE}) returns true</li>
         * <li>{@link #PROFILE}.is({@link #PROFILE_SUMMARY}) returns false</li>
         * <li>{@link #PROFILE_LATEST_POSTS}.is({@link #PROFILE}) returns true</li>
         * <li>{@link #PROFILE}.is({@link #PROFILE_LATEST_POSTS}) returns false</li>
         * <li>{@link #PROFILE_STATS}.is({@link #PROFILE}) returns true</li>
         * <li>{@link #PROFILE}.is({@link #PROFILE_STATS}) returns false</li>
         * <li>{@link #DOWNLOADS_CATEGORY}.is({@link #DOWNLOADS}) returns true</li>
         * <li>{@link #DOWNLOADS}.is({@link #DOWNLOADS_CATEGORY}) returns false</li>
         * <li>{@link #DOWNLOADS_FILE}.is({@link #DOWNLOADS}) returns true</li>
         * <li>{@link #DOWNLOADS}.is({@link #DOWNLOADS_FILE}) returns false</li></ul>
         *
         * @param other another Target
         * @return true if <b>enums</b> are equal, false otherwise
         */
        public boolean is(PageCategory other) {
            return ((this == PROFILE_LATEST_POSTS || this == PROFILE_STATS || this == PROFILE_SUMMARY)
                    && other == PROFILE)
                    || ((this == DOWNLOADS_FILE || this == DOWNLOADS_CATEGORY) && other == DOWNLOADS)
                    || (this != NOT_THMMY && other == THMMY)
                    || this == other;
        }
    }

    /**
     * Simple method the checks whether a url's target is thmmy or not.
     *
     * @param uri url to check
     * @return true if url is pointing to thmmy, false otherwise
     */
    public static boolean isThmmy(Uri uri) {
        return resolvePageCategory(uri) != PageCategory.NOT_THMMY;
    }

    /**
     * This method is used to determine a url's target.
     *
     * @param uri url to resolve
     * @return resolved target
     */
    public static PageCategory resolvePageCategory(Uri uri) {
        final String host = uri.getHost();
        final String uriString = uri.toString();

        if (Objects.equals(uriString, "http://thmmy.gr")
                || Objects.equals(uriString, "https://thmmy.gr")) return PageCategory.INDEX;
        if (Objects.equals(host, "www.thmmy.gr")) {
            if (uriString.contains("topic=")) return PageCategory.TOPIC;
            else if (uriString.contains("board=")) return PageCategory.BOARD;
            else if (uriString.contains("action=profile")) {
                if (uriString.contains(";sa=showPosts"))
                    return PageCategory.PROFILE_LATEST_POSTS;
                else if (uriString.contains(";sa=statPanel"))
                    return PageCategory.PROFILE_STATS;
                else return PageCategory.PROFILE_SUMMARY;
            } else if (uriString.contains("action=unread"))
                return PageCategory.UNREAD_POSTS;
            else if (uriString.contains("action=tpmod;dl=item"))
                return PageCategory.DOWNLOADS_FILE;
            else if (uriString.contains("action=tpmod;dl"))
                return PageCategory.DOWNLOADS_CATEGORY;
            else if (uriString.contains("action=forum") || Objects.equals(uriString, "www.thmmy.gr")
                    || Objects.equals(uriString, "http://www.thmmy.gr")
                    || Objects.equals(uriString, "https://www.thmmy.gr")
                    || Objects.equals(uriString, "https://www.thmmy.gr/smf/index.php"))
                return PageCategory.INDEX;
            Timber.v("Unknown thmmy link found, link: " + uriString);
            return PageCategory.UNKNOWN_THMMY;
        }
        return PageCategory.NOT_THMMY;
    }

    public static String getBoardId(String boardUrl) {
        if (resolvePageCategory(Uri.parse(boardUrl)) == PageCategory.BOARD) {
            String returnString = boardUrl.substring(boardUrl.indexOf("board=") + 6);
            if (returnString.contains("."))
                returnString = boardUrl.substring(boardUrl.indexOf("board=") + 6, boardUrl.lastIndexOf("."));
            return returnString;
        }
        return null;
    }

    public static String getTopicId(String topicUrl) {
        if (resolvePageCategory(Uri.parse(topicUrl)) == PageCategory.TOPIC) {
            String tmp = topicUrl.substring(topicUrl.indexOf("topic=") + 6);
            return tmp.substring(0, tmp.indexOf("."));
        }
        return null;
    }
}
