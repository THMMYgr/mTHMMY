package gr.thmmy.mthmmy.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.Post;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import okhttp3.Request;
import okhttp3.Response;

public class TopicActivity extends BaseActivity {

    private CustomRecyclerView recyclerView;
    private TopicAdapter topicAdapter;
    private ProgressBar progressBar;

    private List<Post> postsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        Bundle extras = getIntent().getExtras();
        ActionBar actionbar = getSupportActionBar();
        if(actionbar!=null)
            actionbar.setTitle(extras.getString("TOPIC_TITLE"));

        postsList = new ArrayList<>();

        topicAdapter = new TopicAdapter();

        recyclerView = (CustomRecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(findViewById(R.id.list).getContext()));
        recyclerView.setAdapter(topicAdapter);

        new TopicTask().execute(extras.getString("TOPIC_URL"));

    }


    private class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_topic_post_row, parent, false);
            return new ViewHolder(view);
        }



        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.mAuthorView.setText(postsList.get(position).getAuthor());
            holder.mDateTimeView.setText(postsList.get(position).getDateTime());
            holder.mContentView.loadData(postsList.get(position).getContent(), "text/html", null);

//            holder.topic = recentList.get(position);
//
//            holder.mView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    if (null != mListener) {
//                        // Notify the active callbacks interface (the activity, if the
//                        // fragment is attached to one) that an item has been selected.
//                        mListener.onFragmentInteraction(holder.topic);  //?
//
//                    }
//
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return postsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mAuthorView;
            public final WebView mContentView;
            public final TextView mDateTimeView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mAuthorView = (TextView) view.findViewById(R.id.author);
                mContentView = (WebView) view.findViewById(R.id.content);
                mDateTimeView = (TextView) view.findViewById(R.id.dateTime);
            }

        }
    }



//---------------------------------------TOPIC ASYNC TASK-------------------------------------------

    public class TopicTask extends AsyncTask<String, Void, Boolean>
    {
        private static final String TAG="TopicTask";
        private String pageLink;

        private Document document;


        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        protected Boolean doInBackground(String... strings)
        {
            pageLink = strings[0];

            Request request = new Request.Builder()
                    .url(pageLink)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                return parse(document);
            } catch (SSLHandshakeException e) {
                Log.w(TAG, "Certificate problem (please switch to unsafe connection).");
                return false;

            } catch (Exception e) {
                Log.e("TAG", "ERROR", e);
                return false;
            }

        }



        protected void onPostExecute(Boolean result)
        {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            topicAdapter.notifyDataSetChanged();
        }

        private boolean parse(Document document)
        {
            return true;

        }


    }
}
