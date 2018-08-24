package gr.thmmy.mthmmy.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import gr.thmmy.mthmmy.R;

public class EditorView extends LinearLayout {

    private EditText editText;
    private AppCompatImageButton emojiButton;
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
        LayoutInflater.from(context).inflate(R.layout.editor_view, this, true);
        setOrientation(VERTICAL);

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
        findViewById(R.id.bold_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[b][/b]");
            editText.setSelection(editText.getSelectionStart() - 4);
        });
        findViewById(R.id.italic_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[i][/i]");
            editText.setSelection(editText.getSelectionStart() - 4);
        });
        findViewById(R.id.underline_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[u][/u]");
            editText.setSelection(editText.getSelectionStart() - 4);
        });
        findViewById(R.id.strikethrough_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[s][/s]");
            editText.setSelection(editText.getSelectionStart() - 4);
        });
        // TODO: popup menu for colors
        findViewById(R.id.text_color_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[color=][/color]");
            editText.setSelection(editText.getSelectionStart() - 8);
        });
        findViewById(R.id.text_size_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[size=10pt][/size]");
            editText.setSelection(editText.getSelectionStart() - 7);
        });
        findViewById(R.id.align_left_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[left][/left]");
            editText.setSelection(editText.getSelectionStart() - 7);
        });
        findViewById(R.id.align_center_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[center][/center]");
            editText.setSelection(editText.getSelectionStart() - 9);
        });
        findViewById(R.id.align_right_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[right][/right]");
            editText.setSelection(editText.getSelectionStart() - 8);
        });
        findViewById(R.id.math_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[tex][/tex]");
            editText.setSelection(editText.getSelectionStart() - 6);
        });
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
