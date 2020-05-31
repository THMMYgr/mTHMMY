package gr.thmmy.mthmmy.views.editorview;

public interface IEmojiKeyboard {
    /**
     * Hide keyboard
     */
    void hide();

    /**
     * Check if keyboard is visible
     * @return true, if {@link EmojiKeyboard#getVisibility()} returns View.VISIBLE, otherwise false
     */
    boolean isVisible();

    /**
     * Callback to the keyboard when {@link EditorView#emojiButton} is clicked
     * @return whether the keyboard became visible or not
     */
    boolean onEmojiButtonToggle();

    /**
     * Callback to create input connection with {@link EmojiInputField}
     * @param emojiInputField the connected input field
     */
    void onEmojiInputFieldFocused(EmojiInputField emojiInputField);

    /**
     * Persist a set of all input fields to update all of them when visibility changes
     * @param emojiInputField the input field to be added
     */
    void registerEmojiInputField(EmojiInputField emojiInputField);
}
