package gr.thmmy.mthmmy.editorview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import java.util.Objects;

import gr.thmmy.mthmmy.R;

public class EditorView extends LinearLayout {

    private SparseArray<String> colors = new SparseArray<>();

    private TextInputLayout edittextWrapper;
    private TextInputEditText editText;
    private AppCompatImageButton emojiButton;
    private AppCompatImageButton submitButton;
    private EmojiKeyboard.EmojiKeyboardOwner emojiKeyboardOwner;

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
            editText.setHint(a.getString(R.styleable.EditorView_hint));
        } finally {
            a.recycle();
        }

        // without this, the editor gets default window background
        Drawable background = getBackground();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setBackground(background);
        }

        emojiButton = findViewById(R.id.emoji_keyboard_button);

        editText.setOnTouchListener((v, event) -> {
            if (emojiKeyboardOwner.isEmojiKeyboardVisible()) return true;
            return false;
        });

        emojiButton.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            assert imm != null;
            if (emojiKeyboardOwner.isEmojiKeyboardVisible()) {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                emojiButton.setImageResource(R.drawable.ic_tag_faces_24dp);
            } else {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                view.clearFocus();
                emojiButton.setImageResource(R.drawable.ic_keyboard_24dp);
            }
            emojiKeyboardOwner.setEmojiKeyboardVisible(!emojiKeyboardOwner.isEmojiKeyboardVisible());
        });

        submitButton = findViewById(R.id.submit_button);
        findViewById(R.id.bold_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[b]");
            getText().insert(editText.getSelectionEnd(), "[/b]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 4);
        });
        findViewById(R.id.italic_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[i]");
            getText().insert(editText.getSelectionEnd(), "[/i]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 4);
        });
        findViewById(R.id.underline_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[u]");
            getText().insert(editText.getSelectionEnd(), "[/u]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 4);
        });
        findViewById(R.id.strikethrough_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[s]");
            getText().insert(editText.getSelectionEnd(), "[/s]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 4);
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
            Context wrapper = new ContextThemeWrapper(view.getContext(), R.style.PopupWindow);
            PopupWindow popupWindow = new PopupWindow(wrapper);
            popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
            popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);
            ScrollView colorPickerScrollview = (ScrollView) LayoutInflater.from(context).inflate(R.layout.editor_view_color_picker, null);
            LinearLayout colorPicker = (LinearLayout) colorPickerScrollview.getChildAt(0);
            popupWindow.setContentView(colorPickerScrollview);
            for (int i = 0; i < colorPicker.getChildCount(); i++) {
                colorPicker.getChildAt(i).setOnClickListener(v -> {
                    boolean hadTextSelection = editText.hasSelection();
                    getText().insert(editText.getSelectionStart(), "[color=" + colors.get(v.getId()) + "]");
                    getText().insert(editText.getSelectionEnd(), "[/color]");
                    editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 8);
                    popupWindow.dismiss();
                });
            }
            popupWindow.showAsDropDown(view);
        });
        findViewById(R.id.text_size_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[size=10pt]");
            getText().insert(editText.getSelectionEnd(), "[/size]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 7);
        });
        findViewById(R.id.font_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[font=Verdana]");
            getText().insert(editText.getSelectionEnd(), "[/font]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 7);
        });
        findViewById(R.id.unordered_list_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[list]\n[li]");
            getText().insert(editText.getSelectionEnd(), "[/li]\n[li][/li]\n[/list]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() - 13 : editText.getSelectionStart() - 23);
        });
        findViewById(R.id.align_left_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[left]");
            getText().insert(editText.getSelectionEnd(), "[/left]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 7);
        });
        findViewById(R.id.align_center_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[center]");
            getText().insert(editText.getSelectionEnd(), "[/center]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 9);
        });
        findViewById(R.id.align_right_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[right]");
            getText().insert(editText.getSelectionEnd(), "[/right]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 8);
        });
        findViewById(R.id.link_button).setOnClickListener(view -> {
            LinearLayout dialogBody = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.dialog_create_link, null);
            TextInputLayout linkUrl = dialogBody.findViewById(R.id.link_url_input);
            linkUrl.setOnClickListener(view1 -> linkUrl.setError(null));
            TextInputLayout linkText = dialogBody.findViewById(R.id.link_text_input);
            linkText.setOnClickListener(view2 -> linkText.setError(null));
            boolean hadTextSelection = editText.hasSelection();
            int start = editText.getSelectionStart(), end = editText.getSelectionEnd();
            if (editText.hasSelection()) {
                linkText.getEditText().setText(
                        editText.getText().toString().substring(editText.getSelectionStart(), editText.getSelectionEnd()));
            }
            new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyleAccent)
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

                        if (hadTextSelection) editText.getText().delete(start, end);
                        getText().insert(editText.getSelectionStart(), "[url=" +
                                Objects.requireNonNull(linkUrl.getEditText()).getText().toString() + "]" +
                                Objects.requireNonNull(linkText.getEditText()).getText().toString() + "[/url]");
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        });
        findViewById(R.id.quote_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[quote]");
            getText().insert(editText.getSelectionEnd(), "[/quote]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 8);
        });
        findViewById(R.id.code_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[code]");
            getText().insert(editText.getSelectionEnd(), "[/code]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 7);
        });
        findViewById(R.id.math_button).setOnClickListener(view -> {
            boolean hadTextSelection = editText.hasSelection();
            getText().insert(editText.getSelectionStart(), "[tex]");
            getText().insert(editText.getSelectionEnd(), "[/tex]");
            editText.setSelection(hadTextSelection ? editText.getSelectionEnd() : editText.getSelectionStart() - 6);
        });
    }

    public TextInputEditText getEditText() {
        return editText;
    }

    public Editable getText() {
        return editText.getText();
    }

    public void setText(Editable text) {
        editText.setText(text);
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

    public void updateEmojiKeyboardVisibility() {
        if (emojiKeyboardOwner.isEmojiKeyboardVisible())
            emojiButton.setImageResource(R.drawable.ic_keyboard_24dp);
        else
            emojiButton.setImageResource(R.drawable.ic_tag_faces_24dp);
    }
}
