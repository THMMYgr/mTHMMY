package gr.thmmy.mthmmy.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputConnection;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.utils.EditorView;
import gr.thmmy.mthmmy.utils.EmojiKeyboard;

public class TestEditView extends AppCompatActivity implements EmojiKeyboard.EmojiKeyboardOwner {
    EmojiKeyboard emojiKeyboard;
    EditorView editorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_edit_view);

        editorView = findViewById(R.id.editor_view);
        emojiKeyboard = findViewById(R.id.emoji_keyboard);

        InputConnection ic = editorView.getInputConnection();
        emojiKeyboard.setInputConnection(ic);
        editorView.setEmojiKeyboardOwner(this);
    }

    @Override
    public void setEmojiKeyboardVisible(boolean visible) {
        emojiKeyboard.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (emojiKeyboard.getVisibility() == View.VISIBLE) {
            emojiKeyboard.setVisibility(View.GONE);
            editorView.notifyKeyboardVisibility(false);
        } else {
            super.onBackPressed();
        }
    }
}
