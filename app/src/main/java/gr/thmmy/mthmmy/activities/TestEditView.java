package gr.thmmy.mthmmy.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.utils.EmojiKeyboard;

public class TestEditView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_edit_view);

        EditText testEdittext = (EditText) findViewById(R.id.test_edittext);
        EmojiKeyboard emojiKeyboard = (EmojiKeyboard) findViewById(R.id.emoji_keyboard);

        testEdittext.setRawInputType(InputType.TYPE_CLASS_TEXT);
        testEdittext.setTextIsSelectable(true);
        InputConnection ic = testEdittext.onCreateInputConnection(new EditorInfo());
        emojiKeyboard.setInputConnection(ic);
    }
}
