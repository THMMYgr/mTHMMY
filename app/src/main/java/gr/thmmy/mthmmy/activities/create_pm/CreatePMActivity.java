package gr.thmmy.mthmmy.activities.create_pm;

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
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.ExternalAsyncTask;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class CreatePMActivity extends BaseActivity implements ExternalAsyncTask.OnTaskStartedListener, ExternalAsyncTask.OnTaskFinishedListener<Boolean> {

    private MaterialProgressBar progressBar;
    private EditorView contentEditor;
    private TextInputLayout subjectInput;
    private EmojiKeyboard emojiKeyboard;
    private String username, sendPmUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pm);

        Intent callingIntent = getIntent();
        username = callingIntent.getStringExtra(ProfileActivity.BUNDLE_PROFILE_USERNAME);
        sendPmUrl = callingIntent.getStringExtra(ProfileActivity.BUNDLE_SEND_PM_URL);

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Create topic");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        progressBar = findViewById(R.id.progressBar);

        emojiKeyboard = findViewById(R.id.emoji_keyboard);

        subjectInput = findViewById(R.id.subject_input);
        subjectInput.getEditText().setRawInputType(InputType.TYPE_CLASS_TEXT);
        subjectInput.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);

        contentEditor = findViewById(R.id.main_content_editorview);
        contentEditor.setEmojiKeyboard(emojiKeyboard);
        emojiKeyboard.registerEmojiInputField(contentEditor);
        contentEditor.setOnSubmitListener(v -> {
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

            SendPMTask sendPMTask = new SendPMTask(includeAppSignature);
            sendPMTask.setOnTaskStartedListener(this);
            sendPMTask.setOnTaskFinishedListener(this);
            sendPMTask.execute(sendPmUrl, subjectInput.getEditText().getText().toString(),
                    contentEditor.getText().toString());
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
    public void onTaskStarted() {
        Timber.i("New pm started being sent");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskFinished(Boolean success) {
        progressBar.setVisibility(View.INVISIBLE);
        if (success) {
            Timber.i("New pm sent successfully");
            Toast.makeText(this, "Personal message sent successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Timber.w("Failed to send pm");
            Toast.makeText(getBaseContext(), "Failed to send PM. Check your connection", Toast.LENGTH_LONG).show();
        }
    }
}
