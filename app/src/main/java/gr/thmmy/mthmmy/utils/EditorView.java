package gr.thmmy.mthmmy.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.Objects;

import gr.thmmy.mthmmy.R;

public class EditorView extends LinearLayout {

    private SparseArray<String> colors = new SparseArray<>();

    private TextInputLayout edittextWrapper;
    private TextInputEditText editText;
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

        edittextWrapper = findViewById(R.id.editor_edittext_wrapper);
        editText = findViewById(R.id.editor_edittext);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EditorView, 0, 0);
        try {
            edittextWrapper.setHint(a.getString(R.styleable.EditorView_hint));
        } finally {
            a.recycle();
        }

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

        colors.append(R.id.black, "black");
        colors.append(R.id.red, "red");
        colors.append(R.id.yellow, "yellow");
        colors.append(R.id.pink, "pink");
        colors.append(R.id.green, "green");
        colors.append(R.id.orange, "orange");
        colors.append(R.id.purple, "purple");
        colors.append(R.id.blue, "blue");
        colors.append(R.id.beige, "beige");
        colors.append(R.id.brown, "brown");
        colors.append(R.id.teal, "teal");
        colors.append(R.id.navy, "navy");
        colors.append(R.id.maroon, "maroon");
        colors.append(R.id.lime_green, "limegreen");

        findViewById(R.id.text_color_button).setOnClickListener(view -> {
            PopupWindow popupWindow = new PopupWindow(view.getContext());
            popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
            popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);
            LinearLayout colorPicker = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.editor_view_color_picker, null);
            popupWindow.setContentView(colorPicker);
            for (int i = 0; i < colorPicker.getChildCount(); i++) {
                colorPicker.getChildAt(i).setOnClickListener(v -> {
                    if (editText.hasSelection())
                        editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
                    getText().insert(editText.getSelectionStart(), "[color=" + colors.get(v.getId()) + "][/color]");
                    editText.setSelection(editText.getSelectionStart() - 8);
                    popupWindow.dismiss();
                });
            }
            popupWindow.showAsDropDown(view);
        });
        findViewById(R.id.text_size_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[size=10pt][/size]");
            editText.setSelection(editText.getSelectionStart() - 7);
        });
        findViewById(R.id.font_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[font=Verdana][/font]");
            editText.setSelection(editText.getSelectionStart() - 7);
        });
        findViewById(R.id.unordered_list_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[list]\n[li][/li]\n[li][/li]\n[/list]");
            editText.setSelection(editText.getSelectionStart() - 23);
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
        findViewById(R.id.link_button).setOnClickListener(view -> {
            LinearLayout dialogBody = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.dialog_create_link, null);
            TextInputLayout linkUrl = dialogBody.findViewById(R.id.link_url_input);
            linkUrl.setOnClickListener(view1 -> linkUrl.setError(null));
            TextInputLayout linkText = dialogBody.findViewById(R.id.link_text_input);
            linkText.setOnClickListener(view2 -> linkText.setError(null));
            new AlertDialog.Builder(context, R.style.AppTheme_Dark_Dialog)
                    .setTitle(R.string.dialog_create_link_title)
                    .setView(dialogBody)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        if (TextUtils.isEmpty(Objects.requireNonNull(linkUrl.getEditText()).getText().toString())) {
                            linkUrl.setError(context.getString(R.string.input_field_required));
                            return;
                        }
                        if (TextUtils.isEmpty(Objects.requireNonNull(linkText.getEditText()).getText().toString())) {
                            linkUrl.setError(context.getString(R.string.input_field_required));
                            return;
                        }

                        if (editText.hasSelection())
                            editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
                        getText().insert(editText.getSelectionStart(), "[url=" +
                                Objects.requireNonNull(linkUrl.getEditText()).getText().toString() + "]" +
                                Objects.requireNonNull(linkText.getEditText()).getText().toString() + "[/url]");
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        });
        findViewById(R.id.quote_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[quote][/quote]");
            editText.setSelection(editText.getSelectionStart() - 8);
        });
        findViewById(R.id.code_button).setOnClickListener(view -> {
            if (editText.hasSelection())
                editText.getText().delete(editText.getSelectionStart(), editText.getSelectionEnd());
            getText().insert(editText.getSelectionStart(), "[code][/code]");
            editText.setSelection(editText.getSelectionStart() - 7);
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

    public void setError(@Nullable CharSequence text) {
        edittextWrapper.setError(text);
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
