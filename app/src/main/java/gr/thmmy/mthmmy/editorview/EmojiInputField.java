package gr.thmmy.mthmmy.editorview;

import android.view.inputmethod.InputConnection;

public interface EmojiInputField {
    void onKeyboardVisibilityChange(boolean visible);
    InputConnection getInputConnection();
}
