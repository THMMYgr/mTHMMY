package gr.thmmy.mthmmy.views.editorview;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import gr.thmmy.mthmmy.R;
import timber.log.Timber;

public class EditorView extends LinearLayout implements EmojiInputField {

    private static final int ANIMATION_DURATION = 200;
    private SparseArray<String> colors = new SparseArray<>();

    private TextInputLayout edittextWrapper;
    private TextInputEditText editText;
    private AppCompatImageButton emojiButton;
    private AppCompatImageButton submitButton;
    private IEmojiKeyboard emojiKeyboard;
    private RecyclerView formatButtonsRecyclerview;

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

    @SuppressLint("SetTextI18n")
    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.editor_view, this, true);
        setOrientation(VERTICAL);

        formatButtonsRecyclerview = findViewById(R.id.buttons_recyclerview);
        edittextWrapper = findViewById(R.id.editor_edittext_wrapper);
        editText = findViewById(R.id.editor_edittext);
        editText.setOnFocusChangeListener((view, focused) -> {
            if (focused) emojiKeyboard.onEmojiInputFieldFocused(EditorView.this);
        });
        edittextWrapper.setOnFocusChangeListener((view, focused) -> {
            if (focused) emojiKeyboard.onEmojiInputFieldFocused(EditorView.this);
        });
        editText.setOnClickListener(view -> {
            if (!emojiKeyboard.isVisible()) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                requestEditTextFocus();
            }
        });
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

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

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float itemWidth = getResources().getDimension(R.dimen.editor_format_button_size) +
                getResources().getDimension(R.dimen.editor_format_button_margin_between);
        int columns = (int) Math.floor(displayMetrics.widthPixels / itemWidth);
        formatButtonsRecyclerview.setLayoutManager(new GridLayoutManager(context, columns));
    formatButtonsRecyclerview.setAdapter(
        new FormatButtonsAdapter(
            (view, drawableId) -> {
              boolean hadTextSelection;
              switch (drawableId) {
                case R.drawable.ic_format_bold:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[b]");
                  getText().insert(editText.getSelectionEnd(), "[/b]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 4);
                  break;
                case R.drawable.ic_format_italic:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[i]");
                  getText().insert(editText.getSelectionEnd(), "[/i]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 4);
                  break;
                case R.drawable.ic_format_underlined:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[u]");
                  getText().insert(editText.getSelectionEnd(), "[/u]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 4);
                  break;
                case R.drawable.ic_strikethrough_s:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[s]");
                  getText().insert(editText.getSelectionEnd(), "[/s]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 4);
                  break;
                case R.drawable.ic_format_color_text:
                  int selectionStart = editText.getSelectionStart();
                  int selectionEnd = editText.getSelectionEnd();
                  PopupWindow popupWindow = new PopupWindow(view.getContext());
                  popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
                  popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
                  popupWindow.setFocusable(true);
                  ScrollView colorPickerScrollview =
                      (ScrollView)
                          LayoutInflater.from(context)
                              .inflate(R.layout.editor_view_color_picker, null);
                  LinearLayout colorPicker = (LinearLayout) colorPickerScrollview.getChildAt(0);
                  popupWindow.setContentView(colorPickerScrollview);
                  for (int i = 0; i < colorPicker.getChildCount(); i++) {
                    TextView child = (TextView) colorPicker.getChildAt(i);
                    child.setOnClickListener(
                        v -> {
                          boolean hadTextSelection2 = editText.hasSelection();
                          getText()
                              .insert(
                                  editText.getSelectionStart(),
                                  "[color=" + colors.get(v.getId()) + "]");
                          getText().insert(editText.getSelectionEnd(), "[/color]");
                          editText.setSelection(
                              hadTextSelection2
                                  ? editText.getSelectionEnd()
                                  : editText.getSelectionStart() - 8);
                          popupWindow.dismiss();
                        });
                  }
                  popupWindow.showAsDropDown(view);
                  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    new AsyncTask<Void, Void, Void>() {
                      @Override
                      protected Void doInBackground(Void... voids) {
                        try {
                          Thread.sleep(100);
                        } catch (InterruptedException e) {
                          Timber.e(e);
                        }
                        return null;
                      }

                      @Override
                      protected void onPostExecute(Void aVoid) {
                        editText.setSelection(selectionStart, selectionEnd);
                      }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                  }
                  break;
                case R.drawable.ic_format_size:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[size=10pt]");
                  getText().insert(editText.getSelectionEnd(), "[/size]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 7);
                  break;
                case R.drawable.ic_text_format:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[font=Verdana]");
                  getText().insert(editText.getSelectionEnd(), "[/font]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 7);
                  break;
                case R.drawable.ic_format_list_bulleted:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[list]\n[li]");
                  getText().insert(editText.getSelectionEnd(), "[/li]\n[li][/li]\n[/list]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd() - 13
                          : editText.getSelectionStart() - 23);
                  break;
                case R.drawable.ic_format_align_left:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[left]");
                  getText().insert(editText.getSelectionEnd(), "[/left]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 7);
                  break;
                case R.drawable.ic_format_align_center:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[center]");
                  getText().insert(editText.getSelectionEnd(), "[/center]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 9);
                  break;
                case R.drawable.ic_format_align_right:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[right]");
                  getText().insert(editText.getSelectionEnd(), "[/right]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 8);
                  break;
                case R.drawable.ic_insert_link:
                  LinearLayout dialogBody =
                      (LinearLayout)
                          LayoutInflater.from(context).inflate(R.layout.dialog_create_link, null);
                  TextInputLayout linkUrl = dialogBody.findViewById(R.id.link_url_input);
                  linkUrl.setOnClickListener(view1 -> linkUrl.setError(null));
                  TextInputLayout linkText = dialogBody.findViewById(R.id.link_text_input);
                  linkText.setOnClickListener(view2 -> linkText.setError(null));
                  hadTextSelection = editText.hasSelection();
                  int start = editText.getSelectionStart(), end = editText.getSelectionEnd();
                  if (editText.hasSelection()) {
                    linkText
                        .getEditText()
                        .setText(
                            editText
                                .getText()
                                .toString()
                                .substring(
                                    editText.getSelectionStart(), editText.getSelectionEnd()));
                  }
                  AlertDialog linkDialog =
                      new AlertDialog.Builder(context, R.style.AppTheme_Dark_Dialog)
                          .setTitle(R.string.dialog_create_link_title)
                          .setView(dialogBody)
                          .setPositiveButton(R.string.ok, null)
                          .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                          .create();
                  linkDialog.setOnShowListener(
                      dialogInterface -> {
                        Button button = linkDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(
                            view12 -> {
                              if (TextUtils.isEmpty(
                                  Objects.requireNonNull(linkUrl.getEditText())
                                      .getText()
                                      .toString())) {
                                linkUrl.setError(context.getString(R.string.input_field_required));
                                return;
                              }

                              if (hadTextSelection) editText.getText().delete(start, end);
                              if (!TextUtils.isEmpty(linkText.getEditText().getText())) {
                                getText()
                                    .insert(
                                        editText.getSelectionStart(),
                                        "[url="
                                            + linkUrl.getEditText().getText().toString()
                                            + "]"
                                            + linkText.getEditText().getText().toString()
                                            + "[/url]");
                              } else
                                getText()
                                    .insert(
                                        editText.getSelectionStart(),
                                        "[url]"
                                            + linkUrl.getEditText().getText().toString()
                                            + "[/url]");
                              linkDialog.dismiss();
                            });
                      });
                  linkDialog.show();
                  break;
                case R.drawable.ic_format_quote:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[quote]");
                  getText().insert(editText.getSelectionEnd(), "[/quote]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 8);
                  break;
                case R.drawable.ic_code:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[code]");
                  getText().insert(editText.getSelectionEnd(), "[/code]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 7);
                  break;
                case R.drawable.ic_functions:
                  hadTextSelection = editText.hasSelection();
                  getText().insert(editText.getSelectionStart(), "[tex]");
                  getText().insert(editText.getSelectionEnd(), "[/tex]");
                  editText.setSelection(
                      hadTextSelection
                          ? editText.getSelectionEnd()
                          : editText.getSelectionStart() - 6);
                  break;
                default:
                  throw new IllegalArgumentException("Unknown format button click");
              }
            }));

        emojiButton.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //cache selection. For some reason it gets reset sometimes
            int selectionStart = editText.getSelectionStart();
            int selectionEnd = editText.getSelectionStart();
            if (emojiKeyboard.onEmojiButtonToggle()) {
                //prevent system keyboard from appearing when clicking the edittext
                editText.setTextIsSelectable(true);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
            else {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
            editText.setSelection(selectionStart, selectionEnd);
        });

        submitButton = findViewById(R.id.submit_button);
    }

    public void setEmojiKeyboard(IEmojiKeyboard emojiKeyboard) {
        this.emojiKeyboard = emojiKeyboard;
    }

    public void showMarkdownOnfocus() {
        edittextWrapper.setOnClickListener(view -> {
            showMarkdown();
        });
        editText.setOnClickListener(view -> {
            if (!emojiKeyboard.isVisible()) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            } else {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                requestEditTextFocus();
            }
            showMarkdown();
        });
        edittextWrapper.setOnFocusChangeListener((view, b) -> {
            if (b) {
                emojiKeyboard.onEmojiInputFieldFocused(EditorView.this);
                showMarkdown();
            } else hideMarkdown();
        });
        editText.setOnFocusChangeListener((view, b) -> {
            if (b) {
                emojiKeyboard.onEmojiInputFieldFocused(EditorView.this);
                showMarkdown();
            } else hideMarkdown();
        });
    }

    /**
     * Animates the hiding of the markdown options.
     *
     */
    public void hideMarkdown() {
        if (formatButtonsRecyclerview.getVisibility() == GONE) return;
        ViewPropertyAnimator animator = formatButtonsRecyclerview.animate()
                .translationY(formatButtonsRecyclerview.getHeight())
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(ANIMATION_DURATION);

        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                formatButtonsRecyclerview.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animator.start();
    }

    /**
     * Animates the showing of the markdown options.
     *
     */
    public void showMarkdown() {
        if (formatButtonsRecyclerview.getVisibility() == VISIBLE) return;
        ViewPropertyAnimator animator = formatButtonsRecyclerview.animate()
                .translationY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(ANIMATION_DURATION);

        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                formatButtonsRecyclerview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animator.start();
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

    public boolean requestEditTextFocus() {
        emojiKeyboard.onEmojiInputFieldFocused(EditorView.this);
        return editText.requestFocus();
    }

    @Override
    public void onKeyboardVisibilityChange(boolean visible) {
        if (visible) {
            emojiButton.setImageResource(R.drawable.ic_keyboard_24dp);
        } else {
            emojiButton.setImageResource(R.drawable.ic_tag_faces_24dp);
        }
    }

    @Override
    public InputConnection getInputConnection() {
        return editText.onCreateInputConnection(new EditorInfo());
    }
}
