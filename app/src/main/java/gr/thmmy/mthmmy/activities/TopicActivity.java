package gr.thmmy.mthmmy.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.Post;
import gr.thmmy.mthmmy.utils.CircularNetworkImageView;
import gr.thmmy.mthmmy.utils.ImageController;
import okhttp3.Request;
import okhttp3.Response;

public class TopicActivity extends BaseActivity {

//-----------------------------------------CLASS VARIABLES------------------------------------------

    /* --Post-- */
    private List<Post> postsList;
    private LinearLayout postsLinearLayout;
    /* --Topic's page-- */
    private int thisPage = 1;
    private String base_url = "";
    private int numberOfPages = 1;
    private EditText pageSelect;
    private final SparseArray<String> pagesUrls = new SparseArray<>();
    /* --Topic's page end-- */
    /* --Thumbnail-- */
    private static final int THUMBNAIL_SIZE = 80;
    private ImageLoader imageLoader = ImageController.getInstance().getImageLoader();
    /* --Thumbnail end-- */

    //Other variables
    private ProgressBar progressBar;
    private static final String TAG = "TopicActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Bundle extras = getIntent().getExtras();
        final String topicTitle = getIntent().getExtras().getString("TOPIC_TITLE");

        //Variables initialization
        postsLinearLayout = (LinearLayout) findViewById(R.id.posts_list);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (imageLoader == null)
            imageLoader = ImageController.getInstance().getImageLoader();

        postsList = new ArrayList<>();

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null)
            actionbar.setTitle(topicTitle);

        /* Add page select field to the Action Bar */
        //Edit Action Bar's parameters
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT
                , Gravity.END | Gravity.CENTER_VERTICAL);
        //Initialize layout to be added
        View customNav = LayoutInflater.from(this).inflate(R.layout.topic_page_select, null);
        //Initialize text field
        pageSelect = (EditText) customNav.findViewById(R.id.select_page);
        pageSelect.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { //When page is changed
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    //Get page requested by user
                    int pageRequested = Integer.parseInt(pageSelect.getText().toString());
                    if (pageRequested == thisPage) {
                        Toast.makeText(getBaseContext()
                                , "You already are here!", Toast.LENGTH_LONG).show();
                    } else if (pageRequested >= 1 && pageRequested <= numberOfPages) {
                        //Restart activity with new page
                        Intent intent = getIntent();
                        intent.putExtra("TOPIC_URL", pagesUrls.get(pageRequested - 1));
                        intent.putExtra("TOPIC_TITLE", topicTitle);
                        finish();
                        startActivity(intent);
                    } else { //Invalid page request
                        Toast.makeText(getBaseContext()
                                , "There is no such page!", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            }
        });
        assert actionbar != null;
        actionbar.setCustomView(customNav, lp); //Add feature to Action Bar
        actionbar.setDisplayShowCustomEnabled(true);

        new TopicTask().execute(extras.getString("TOPIC_URL")); //Attempt data parsing
    }

    @Override
    protected void onDestroy() { //When finished cancel whatever request can still be canceled
        super.onDestroy();
        ImageController.getInstance().cancelPendingRequests();
    }

//---------------------------------------TOPIC ASYNC TASK-------------------------------------------
    public class TopicTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask

        //Show a progress bar until done
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        protected Boolean doInBackground(String... strings) {
            Document document;
            base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //This topic's base url
            String pageLink = strings[0]; //This page's url

            Request request = new Request.Builder()
                    .url(pageLink)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                parse(document); //Parse data
                return true;
            } catch (SSLHandshakeException e) {
                Log.w(TAG, "Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Log.e("TAG", "ERROR", e);
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (!result) { //Parse failed!
                //Should never happen
                Toast.makeText(getBaseContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                finish();
            }
            //Parse was successful
            progressBar.setVisibility(ProgressBar.INVISIBLE); //Hide progress bar
            populateLayout(); //Show parsed data
            //Set current page
            pageSelect.setHint(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
        }

        /* Parse method */
        private void parse(Document document) {
            { //Find current page's index
                Elements findCurrentPage = document.select("td:contains(Pages:)>b"); //Contains pages
                for (Element item : findCurrentPage) {
                    if (!item.text().contains("...") //It's not "..."
                            && !item.text().contains("Pages")) { //Nor "Pages"
                        thisPage = Integer.parseInt(item.text());
                        break;
                    }
                }
            }
            { //Find number of pages
                Elements pages = document.select("td:contains(Pages:)>a.navPages"); //Contains all pages
                if (pages.size() != 0) {
                    numberOfPages = thisPage; //Initialize the number
                    for (Element item : pages) { //Just a max
                        if (Integer.parseInt(item.text()) > numberOfPages)
                            numberOfPages = Integer.parseInt(item.text());
                    }
                }
                for (int i = 0; i < numberOfPages; i++) {
                    //Generate each page's url from topic's base url +".15*numberOfPage"
                    pagesUrls.put(i, base_url + "." + String.valueOf(i * 15));
                }
            }

            //Each element is a post's row
            Elements rows = document.select("form[id=quickModForm]>table>tbody>tr:matches(on)");

            for (Element item : rows) { //For every post
                //Variables to pass
                String p_userName, p_thumbnailUrl, p_subject, p_post;
                int p_postNum;

                //Find the Username
                Element userName = item.select("a[title^=View the profile of]").first();
                if (userName == null) { //Deleted profile
                    p_userName = item
                            .select("td:has(div.smalltext:containsOwn(Guest))[style^=overflow]")
                            .first().text();
                    p_userName = p_userName.substring(0, p_userName.indexOf(" Guest"));
                } else
                    p_userName = userName.html();

                //Find thumbnail url
                Element thumbnailUrl = item.select("img.avatar").first();
                p_thumbnailUrl = null; //In case user doesn't have an avatar
                if (thumbnailUrl != null) {
                    p_thumbnailUrl = thumbnailUrl.attr("abs:src");
                }

                //Find subject
                p_subject = item.select("div[id^=subject_]").first().select("a").first().text();

                //Find post's text
                p_post = item.select("div").select(".post").first().html();
                //Add stuff to make it work in WebView
                p_post = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
                        + p_post); //style.css

                //Find post's index number
                Element postNum = item.select("div.smalltext:matches(Reply #)").first();
                if (postNum == null) { //Topic starter
                    p_postNum = 0;
                } else {
                    String tmp_str = postNum.text().substring(9);
                    p_postNum = Integer.parseInt(tmp_str.substring(0, tmp_str.indexOf(" on")));
                }

                //Add new post in postsList
                postsList.add(new Post(p_thumbnailUrl, p_userName, p_subject
                        , p_post, p_postNum));
            }
        }
        /* Parse method end */
    }
//-------------------------------------TOPIC ASYNC TASK END-----------------------------------------

    private void populateLayout() { //Show parsed data
        //Initialize an inflater
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (Post item : postsList) {
            //Inflate a topic post row layout
            View convertView = inflater.inflate(R.layout.activity_topic_post_row
                    , postsLinearLayout, false);

            if (imageLoader == null)
                imageLoader = ImageController.getInstance().getImageLoader();

            //Initialize layout's graphic elements
            CircularNetworkImageView thumbnail = (CircularNetworkImageView) convertView.findViewById(R.id.thumbnail);
            TextView username = (TextView) convertView.findViewById(R.id.username);
            TextView postNum = (TextView) convertView.findViewById(R.id.post_number);
            TextView subject = (TextView) convertView.findViewById(R.id.subject);
            WebView post = (WebView) convertView.findViewById(R.id.post);

            //Avoiding errors about layout having 0 width/height
            thumbnail.setMinimumWidth(1);
            thumbnail.setMinimumHeight(1);
            //Set thumbnail size
            thumbnail.setMaxWidth(THUMBNAIL_SIZE);
            thumbnail.setMaxHeight(THUMBNAIL_SIZE);

            //Thumbnail image set
            if (item.getThumbnailUrl() != null) {
                thumbnail.setImageUrl(item.getThumbnailUrl(), imageLoader);
            }

            //Username set
            username.setText(item.getAuthor());

            //Post's index number set
            if (item.getPostNumber() != 0)
                postNum.setText("#" + item.getPostNumber());

            //Subject set
            subject.setText(item.getSubject());

            //Post's text set
            post.loadDataWithBaseURL("file:///android_asset/", item.getContent(), "text/html", "UTF-8", null);
            post.setEnabled(false);

            //Add view to the linear layout that holds all posts
            postsLinearLayout.addView(convertView);
        }
    }
}
