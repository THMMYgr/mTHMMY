package gr.thmmy.mthmmy.activities.board;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class BoardActivity extends BaseActivity {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    private static final String TAG = "BoardActivity";
    /**
     * The key to use when putting board's url String to {@link BoardActivity}'s Bundle.
     */
    public static final String BUNDLE_BOARD_URL = "BOARD_URL";
    /**
     * The key to use when putting board's title String to {@link BoardActivity}'s Bundle.
     */
    public static final String BUNDLE_BOARD_TITLE = "BOARD_TITLE";

    private MaterialProgressBar progressBar;
    private String boardTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Bundle extras = getIntent().getExtras();
        boardTitle = extras.getString("BOARD_TITLE");

        //Initializes graphics
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(boardTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
    }
}
