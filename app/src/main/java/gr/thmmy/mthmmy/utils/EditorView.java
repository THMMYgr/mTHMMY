package gr.thmmy.mthmmy.utils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import gr.thmmy.mthmmy.R;

public class EditorView extends LinearLayout {

    private EditText editText;
    AppCompatImageButton emojiButton;
    private AppCompatImageButton submitButton;
    private EmojiKeyboard.EmojiKeyboardOwner emojiKeyboardOwner;
    private boolean emojiKeyboardVisible = false;

    public EditorView(Context context) {
        super(context);
        init(context, null);
    }

    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EditorView(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.editor_view, this, true);

        editText = findViewById(R.id.editor_edittext);
        emojiButton = findViewById(R.id.emoji_keyboard_button);

        editText.setOnTouchListener((v, event) -> {
            if (emojiKeyboardVisible) return true;
            return false;
        });

        emojiButton.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            assert imm != null;
            if (emojiKeyboardVisible) {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                emojiButton.setImageResource(R.drawable.ic_tag_faces_grey_24dp);
            } else {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                view.clearFocus();
                emojiButton.setImageResource(R.drawable.ic_keyboard_grey_24dp);
            }
            emojiKeyboardVisible = !emojiKeyboardVisible;
            emojiKeyboardOwner.setEmojiKeyboardVisible(emojiKeyboardVisible);
        });

        submitButton = findViewById(R.id.submit_button);
    }

    public Editable getText() {
        return editText.getText();
    }

    public void setText(CharSequence text) {
        editText.setText(text);
    }

    public void setOnSubmitListener(OnClickListener onSubmitListener) {
        submitButton.setOnClickListener(onSubmitListener);
    }

    public void setEmojiKeyboardOwner(EmojiKeyboard.EmojiKeyboardOwner emojiKeyboardOwner) {
        this.emojiKeyboardOwner = emojiKeyboardOwner;
    }

    public InputConnection getInputConnection() {
        return editText.onCreateInputConnection(new EditorInfo());
    }

    public void notifyKeyboardVisibility(boolean visible) {
        if (visible) {
            emojiButton.setImageResource(R.drawable.ic_keyboard_grey_24dp);
            emojiKeyboardVisible = true;
        } else {
            emojiButton.setImageResource(R.drawable.ic_tag_faces_grey_24dp);
            emojiKeyboardVisible = false;
        }
    }
}
