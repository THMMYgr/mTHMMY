package gr.thmmy.mthmmy.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputConnection;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;

public class CreateContentActivity extends AppCompatActivity implements EmojiKeyboard.EmojiKeyboardOwner {

    EditorView contentEditor;
    EmojiKeyboard emojiKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_content);

        emojiKeyboard = findViewById(R.id.emoji_keyboard);

        contentEditor = findViewById(R.id.main_content_editorview);
        setEmojiKeyboardInputConnection(contentEditor.getInputConnection());
        contentEditor.setEmojiKeyboardOwner(this);
        contentEditor.setOnSubmitListener(v -> {

        });
    }

    @Override
    public void setEmojiKeyboardVisible(boolean visible) {
        emojiKeyboard.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isEmojiKeyboardVisible() {
        return emojiKeyboard.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setEmojiKeyboardInputConnection(InputConnection ic) {
        emojiKeyboard.setInputConnection(ic);
    }

    @Override
    public void onBackPressed() {
        if (emojiKeyboard.getVisibility() == View.VISIBLE) {
            emojiKeyboard.setVisibility(View.GONE);
            contentEditor.updateEmojiKeyboardVisibility();
        } else {
            super.onBackPressed();
        }
    }
}
