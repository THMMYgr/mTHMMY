package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import gr.thmmy.mthmmy.R;

public class EditorView extends RelativeLayout implements KeyboardView.OnKeyboardActionListener {

    public final static int SMILE = 10;

    private EditText editText;
    private AppCompatImageButton submitButton;

    public EditorView(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.editor_view, this, true);

        editText = (EditText) findViewById(R.id.editor_edittext);
        submitButton = (AppCompatImageButton) findViewById(R.id.submit_button);

        /*Keyboard emojiKeyboard = new Keyboard(context, R.xml.emoji_keyboard);
        KeyboardView emojiKeyboardView = (KeyboardView) getChildAt(2);
        emojiKeyboardView.setKeyboard(emojiKeyboard);
        emojiKeyboardView.setPreviewEnabled(false);
        emojiKeyboardView.setOnKeyboardActionListener(this);*/
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

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Editable editable = editText.getText();
        if (editText.hasSelection())
            editable.delete(editText.getSelectionStart(), editText.getSelectionEnd());
        int cursorIndex = editText.getSelectionStart();
        String appendedText = "";
        switch (primaryCode) {
            case SMILE:
                appendedText = "^:)^";
                break;
        }
        editable.insert(cursorIndex, appendedText);
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
