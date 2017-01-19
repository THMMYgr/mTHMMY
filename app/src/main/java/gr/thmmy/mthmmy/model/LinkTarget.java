package gr.thmmy.mthmmy.model;

import android.net.Uri;

import java.util.Objects;

import mthmmy.utils.Report;

/**
 * This class consists exclusively of static classes (enums) and methods (excluding methods of inner
 * classes). It can be used to resolve link targets as to whether they are pointing to the forum and
 * where in the forum they may point.
 */
public class LinkTarget {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "LinkTarget";

    /**
     * An enum describing a link's target by defining the types:<ul>
     * <li>{@link #NOT_THMMY}</li>
     * <li>{@link #THMMY}</li>
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
    public enum Target {
        /**
         * Link doesn't point to thmmy.
         */
        NOT_THMMY,
        /**
         * Link points to thmmy.
         */
        THMMY,
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
         * This method defines a custom equality check for {@link Target} enums. It does not check
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
        public boolean is(Target other) {
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
        return resolveLinkTarget(uri) != Target.NOT_THMMY;
    }

    /**
     * This method is used to determine a url's target.
     *
     * @param uri url to resolve
     * @return resolved target
     */
    public static Target resolveLinkTarget(Uri uri) {
        final String host = uri.getHost();
        final String uriString = uri.toString();

        if (Objects.equals(host, "www.thmmy.gr")) {
            if (uriString.contains("topic=")) return Target.TOPIC;
            else if (uriString.contains("board=")) return Target.BOARD;
            else if (uriString.contains("action=profile")) {
                if (uriString.contains(";sa=showPosts"))
                    return Target.PROFILE_LATEST_POSTS;
                else if (uriString.contains(";sa=statPanel"))
                    return Target.PROFILE_STATS;
                else return Target.PROFILE_SUMMARY;
            } else if (uriString.contains("action=unread"))
                return Target.UNREAD_POSTS;
            else if (uriString.contains("action=tpmod;dl=item"))
                return Target.DOWNLOADS_FILE;
            else if (uriString.contains("action=tpmod;dl"))
                return Target.DOWNLOADS_CATEGORY;
            Report.v(TAG, "Unknown thmmy link found, link: " + uriString);
            return Target.UNKNOWN_THMMY;
        }
        return Target.NOT_THMMY;
    }
}
