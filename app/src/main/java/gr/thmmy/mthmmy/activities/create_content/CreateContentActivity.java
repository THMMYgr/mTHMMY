package gr.thmmy.mthmmy.activities.create_content;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.views.editorview.EditorView;
import gr.thmmy.mthmmy.views.editorview.EmojiKeyboard;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class CreateContentActivity extends BaseActivity implements NewTopicTask.NewTopicTaskCallbacks {

    public final static String EXTRA_NEW_TOPIC_URL = "new-topic-extra";

    private EditorView contentEditor;
    private EmojiKeyboard emojiKeyboard;
    private TextInputLayout subjectInput;
    private MaterialProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_content);

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Create topic");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        progressBar = findViewById(R.id.progressBar);

        Intent callingIntent = getIntent();
        String newTopicUrl = callingIntent.getStringExtra(EXTRA_NEW_TOPIC_URL);

        emojiKeyboard = findViewById(R.id.emoji_keyboard);

        subjectInput = findViewById(R.id.subject_input);
        subjectInput.getEditText().setRawInputType(InputType.TYPE_CLASS_TEXT);
        subjectInput.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);

        contentEditor = findViewById(R.id.main_content_editorview);
        contentEditor.setEmojiKeyboard(emojiKeyboard);
        emojiKeyboard.registerEmojiInputField(contentEditor);
        contentEditor.setOnSubmitListener(v -> {
            if (newTopicUrl != null) {
                if (TextUtils.isEmpty(subjectInput.getEditText().getText())) {
                    subjectInput.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(contentEditor.getText())) {
                    contentEditor.setError("Required");
                    return;
                }
                boolean includeAppSignature = true;
                SessionManager sessionManager = BaseActivity.getSessionManager();
                if (sessionManager.isLoggedIn()) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    includeAppSignature = prefs.getBoolean(SettingsActivity.POSTING_APP_SIGNATURE_ENABLE_KEY, true);
                }
                emojiKeyboard.setVisibility(View.GONE);

                new NewTopicTask(this, includeAppSignature).execute(newTopicUrl, subjectInput.getEditText().getText().toString(),
                        contentEditor.getText().toString());
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (emojiKeyboard.getVisibility() == View.VISIBLE) {
            emojiKeyboard.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNewTopicTaskStarted() {
        Timber.i("New topic creation started");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNewTopicTaskFinished(boolean success) {
        progressBar.setVisibility(View.INVISIBLE);
        if (success) {
            Timber.i("New topic created successfully");
            finish();
        } else {
            Timber.w("New topic creation failed");
            Toast.makeText(getBaseContext(), "Failed to create new topic!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
