package gr.thmmy.mthmmy.activities.create_pm;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.google.android.material.textfield.TextInputLayout;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class CreatePMActivity extends BaseActivity {

    private MaterialProgressBar progressBar;
    private EditorView contentEditor;
    private TextInputLayout subjectInput;
    private EmojiKeyboard emojiKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pm);

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
            // TODO: send pm
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


}
