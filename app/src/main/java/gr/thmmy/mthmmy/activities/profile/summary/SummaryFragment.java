package gr.thmmy.mthmmy.activities.profile.summary;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseFragment;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;


/**
 * A {@link BaseFragment} subclass.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends BaseFragment {
    static final String PROFILE_DOCUMENT = "PROFILE_DOCUMENT";
    private static final String TAG = "SummaryFragment";
    /**
     * {@link ArrayList} of Strings used to hold profile's information. Data are added in {@link ProfileActivity.ProfileTask}.
     */
    private Document profileSummaryDocument;
    private ProfileSummaryTask profileSummaryTask;
    private ArrayList<String> parsedProfileSummaryData;
    private LinearLayout mainContent;
    private MaterialProgressBar progressBar;

    public SummaryFragment() {
        // Required empty public constructor
        this.profileSummaryDocument = Jsoup.parse(getArguments().getString(PROFILE_DOCUMENT));
    }

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Summary.
     */
    public static SummaryFragment newInstance(int sectionNumber, Document profileSummaryDocument) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(PROFILE_DOCUMENT, profileSummaryDocument.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parsedProfileSummaryData = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (parsedProfileSummaryData.isEmpty()) {
            profileSummaryTask = new ProfileSummaryTask();
            profileSummaryTask.execute(profileSummaryDocument);

        }
        Report.d(TAG, "onActivityCreated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (profileSummaryTask != null && profileSummaryTask.getStatus() != AsyncTask.Status.RUNNING)
            profileSummaryTask.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.profile_fragment_summary, container, false);
        progressBar = (MaterialProgressBar) rootView.findViewById(R.id.progressBar);
        mainContent = (LinearLayout) rootView.findViewById(R.id.profile_activity_content);
        populateLayout();
        return rootView;
    }

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a profile page and parsing it's
     * data. {@link AsyncTask#onPostExecute(Object) OnPostExecute} method calls {@link #populateLayout()}
     * to build graphics.
     * <p>
     * <p>Calling ProfileSummaryTask's {@link AsyncTask#execute execute} method needs to have profile's url
     * as String parameter!</p>
     */
    public class ProfileSummaryTask extends AsyncTask<Document, Void, Void> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask

        protected void onPreExecute() {
            progressBar.setVisibility(MaterialProgressBar.VISIBLE);
        }

        protected Void doInBackground(Document... profileSummaryPage) {
            parsedProfileSummaryData = parseProfileSummary(profileSummaryPage[0]);
            return null;
        }

        protected void onPostExecute(Void result) {
            progressBar.setVisibility(MaterialProgressBar.INVISIBLE);
            populateLayout();
        }

        /**
         * Returns an {@link ArrayList} of {@link String}s. This method is used to parse all available
         * information in a user profile.
         * <p>
         * User's thumbnail image url, username and personal text are placed at Array's indexes defined
         * by public constants THUMBNAIL_URL_INDEX, USERNAME_INDEX and PERSONAL_TEXT_INDEX respectively.
         *
         * @param profile {@link Document} object containing this profile's source code
         * @return ArrayList containing this profile's parsed information
         * @see org.jsoup.Jsoup Jsoup
         */
        ArrayList<String> parseProfileSummary(Document profile) {
            //Method's variables
            ArrayList<String> parsedInformation = new ArrayList<>();

            //Contains all summary's rows
            Elements summaryRows = profile.select(".bordercolor > tbody:nth-child(1) > tr:nth-child(2) tr");

            for (Element row : summaryRows) {
                String rowText = row.text(), pHtml = "";

                //Horizontal rule rows
                if (row.select("td").size() == 1)
                    pHtml = "";
                else if (rowText.contains("Signature") || rowText.contains("Υπογραφή")) {
                    //This needs special handling since it may have css
                    { //Fix embedded videos
                        Elements noembedTag = row.select("noembed");
                        ArrayList<String> embededVideosUrls = new ArrayList<>();

                        for (Element _noembed : noembedTag) {
                            embededVideosUrls.add(_noembed.text().substring(_noembed.text()
                                            .indexOf("href=\"https://www.youtube.com/watch?") + 38
                                    , _noembed.text().indexOf("target") - 2));
                        }

                        pHtml = row.html();

                        int tmp_counter = 0;
                        while (pHtml.contains("<embed")) {
                            if (tmp_counter > embededVideosUrls.size())
                                break;
                            pHtml = pHtml.replace(
                                    pHtml.substring(pHtml.indexOf("<embed"), pHtml.indexOf("/noembed>") + 9)
                                    , "<div class=\"embedded-video\">"
                                            + "<a href=\"https://www.youtube.com/"
                                            + embededVideosUrls.get(tmp_counter) + "\" target=\"_blank\">"
                                            + "<img src=\"https://img.youtube.com/vi/"
                                            + embededVideosUrls.get(tmp_counter) + "/default.jpg\" alt=\"\" border=\"0\">"
                                            + "</a>"
                                            //+ "<img class=\"embedded-video-play\" src=\"http://www.youtube.com/yt/brand/media/image/YouTube_light_color_icon.png\">"
                                            + "</div>");
                        }
                    }

                    //Add stuff to make it work in WebView
                    //style.css
                    pHtml = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + pHtml);
                } else if (!rowText.contains("Name") && !rowText.contains("Όνομα")) { //Don't add username twice
                    if (Objects.equals(row.select("td").get(1).text(), ""))
                        continue;
                    //Style parsed information with html
                    pHtml = "<b>" + row.select("td").first().text() + "</b> "
                            + row.select("td").get(1).text();
                }
                parsedInformation.add(pHtml);
            }
            return parsedInformation;
        }
    }

    /**
     * Simple method that builds the UI of a {@link ProfileActivity}.
     * <p>Use this method <b>only after</b> parsing profile's data with
     * {@link gr.thmmy.mthmmy.activities.profile.ProfileActivity.ProfileTask} as it reads from
     * {@link #parsedProfileSummaryData}</p>
     */
    private void populateLayout() {
        for (String profileSummaryRow : parsedProfileSummaryData) {
            if (profileSummaryRow.contains("Signature")
                    || profileSummaryRow.contains("Υπογραφή")) {
                WebView signatureEntry = new WebView(this.getContext());
                signatureEntry.loadDataWithBaseURL("file:///android_asset/", profileSummaryRow, "text/html", "UTF-8", null);
            }
            TextView entry = new TextView(this.getContext());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                entry.setTextColor(getResources().getColor(R.color.primary_text, null));
            } else {
                //noinspection deprecation
                entry.setTextColor(getResources().getColor(R.color.primary_text));

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                entry.setText(Html.fromHtml(profileSummaryRow, Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                entry.setText(Html.fromHtml(profileSummaryRow));
            }

            mainContent.addView(entry);
            Log.d(TAG, "new: " + profileSummaryRow);
        }
    }
}
