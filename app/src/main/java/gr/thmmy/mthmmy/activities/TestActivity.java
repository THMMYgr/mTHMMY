package gr.thmmy.mthmmy.activities;

import androidx.appcompat.app.AppCompatActivity;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.utils.parsing.ThmmyParser;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.TextView;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        String bb = "[b]An [i]elep[u]hant[/i][/b] swi[/u]ms in [s]the[/s] tree";
        SpannableStringBuilder result = ThmmyParser.bb2span(bb);

        TextView bbRaw = findViewById(R.id.bb_raw);
        TextView bb2Text = findViewById(R.id.bb2text);

        bbRaw.setText(bb);
        bb2Text.setText(result);
    }
}
