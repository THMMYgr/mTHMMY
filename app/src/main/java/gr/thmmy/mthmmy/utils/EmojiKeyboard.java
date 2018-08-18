package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageButton;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.GridView;
import android.widget.LinearLayout;

import gr.thmmy.mthmmy.R;

public class EmojiKeyboard extends LinearLayout {

    private SparseArray<String> emojis = new SparseArray<>();

    InputConnection inputConnection;

    public EmojiKeyboard(Context context) {
        this(context, null, 0);
    }

    public EmojiKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmojiKeyboard(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.emoji_keyboard, this, true);

        emojis.append(R.drawable.emoji_smiley, ":)");
        emojis.append(R.drawable.emoji_wink, ";)");
        emojis.append(R.drawable.emoji_cheesy, ":D");
        emojis.append(R.drawable.emoji_grin, ";D");
        // removed repeated angry emoji
        emojis.append(R.drawable.emoji_angry, ">:(");
        emojis.append(R.drawable.emoji_sad, ":(");
        emojis.append(R.drawable.emoji_shocked, ":o");
        emojis.append(R.drawable.emoji_cool, "8))");
        emojis.append(R.drawable.emoji_huh, ":???:");
        emojis.append(R.drawable.emoji_rolleyes, "::)");
        emojis.append(R.drawable.emoji_tongue, ":P");
        emojis.append(R.drawable.emoji_embarrassed, ":-[");
        emojis.append(R.drawable.emoji_lipsrsealed, ":-X");
        emojis.append(R.drawable.emoji_undecided, ":-\\\\");
        emojis.append(R.drawable.emoji_kiss, ":-*");
        emojis.append(R.drawable.emoji_cry, ":'(");
        emojis.append(R.drawable.emoji_heart, "<3");
        // removed repeated lock emoji
        emojis.append(R.drawable.emoji_locked, "^lock^");
        emojis.append(R.drawable.emoji_roll_over, "^rollover^");
        emojis.append(R.drawable.emoji_redface, "^redface^");
        emojis.append(R.drawable.emoji_confused, "^confused^");
        emojis.append(R.drawable.emoji_innocent, "^innocent^");
        emojis.append(R.drawable.emoji_sleep, "^sleep^");
        emojis.append(R.drawable.emoji_lips_sealed, "^sealed^");
        emojis.append(R.drawable.emoji_cool2, "^cool^");
        emojis.append(R.drawable.emoji_crazy, "^crazy^");
        emojis.append(R.drawable.emoji_mad, "^mad^");
        emojis.append(R.drawable.emoji_wav, "^wav^");
        emojis.append(R.drawable.emoji_binkybaby, "^binkybaby^");
        emojis.append(R.drawable.emoji_police, "^police^");
        emojis.append(R.drawable.emoji_dontknow, "^dontknow^");
        //removed repeated angry hot emoji
        emojis.append(R.drawable.emoji_angry_hot, "^angryhot^");
        emojis.append(R.drawable.emoji_foyska, "^fouska^");
        emojis.append(R.drawable.emoji_e10_7_3e, "^sfinaki^");
        emojis.append(R.drawable.emoji_bang_head, "^banghead^");
        emojis.append(R.drawable.emoji_crybaby, "^crybaby^");
        emojis.append(R.drawable.emoji_hello, "^hello^");
        emojis.append(R.drawable.emoji_jerk, "^jerk^");
        emojis.append(R.drawable.emoji_nono, "^nono^");
        emojis.append(R.drawable.emoji_notworthy, "^notworthy^");
        emojis.append(R.drawable.emoji_off_topic, "^off-topic^");
        emojis.append(R.drawable.emoji_puke, "^puke^");
        emojis.append(R.drawable.emoji_shout, "^shout^");
        emojis.append(R.drawable.emoji_slurp, "^slurp^");
        emojis.append(R.drawable.emoji_superconfused, "^superconfused^");
        emojis.append(R.drawable.emoji_superinnocent, "^superinnocent^");
        emojis.append(R.drawable.emoji_cell_phone, "^cellPhone^");
        emojis.append(R.drawable.emoji_idiot, "^idiot^");
        emojis.append(R.drawable.emoji_knuppel, "^knuppel^");
        emojis.append(R.drawable.emoji_tickedoff, "^tickedOff^");
        emojis.append(R.drawable.emoji_peace, "^peace^");
        emojis.append(R.drawable.emoji_suspicious, "^suspicious^");
        emojis.append(R.drawable.emoji_caffine, "^caffine^");
        emojis.append(R.drawable.emoji_argue, "^argue^");
        emojis.append(R.drawable.emoji_banned2, "^banned2^");
        emojis.append(R.drawable.emoji_banned, "^banned^");
        emojis.append(R.drawable.emoji_bath, "^bath^");
        emojis.append(R.drawable.emoji_beg, "^beg^");
        emojis.append(R.drawable.emoji_bluescreen, "^bluescreen^");
        emojis.append(R.drawable.emoji_boil, "^boil^");
        emojis.append(R.drawable.emoji_bye, "^bye^");
        emojis.append(R.drawable.emoji_callmerip, "^callmerip^");
        emojis.append(R.drawable.emoji_carnaval, "^carnaval^");
        emojis.append(R.drawable.emoji_clap, "^clap^");
        emojis.append(R.drawable.emoji_coffeepot, "^coffepot^");
        emojis.append(R.drawable.emoji_crap, "^crap^");
        emojis.append(R.drawable.emoji_curses, "^curses^");
        emojis.append(R.drawable.emoji_funny, "^funny^");
        emojis.append(R.drawable.emoji_guitar1, "^guitar^");
        emojis.append(R.drawable.emoji_icon_kissy, "^kissy^");
        emojis.append(R.drawable.emoji_band, "^band^");
        emojis.append(R.drawable.emoji_ivres, "^ivres^");
        emojis.append(R.drawable.emoji_kaloe, "^kaloe^");
        emojis.append(R.drawable.emoji_kremala, "^kremala^");
        emojis.append(R.drawable.emoji_moon, "^moon^");
        emojis.append(R.drawable.emoji_mopping, "^mopping^");
        emojis.append(R.drawable.emoji_mountza, "^mountza^");
        emojis.append(R.drawable.emoji_pcsleep, "^pcsleep^");
        emojis.append(R.drawable.emoji_pinokio, "^pinokio^");
        emojis.append(R.drawable.emoji_poke, "^poke^");
        emojis.append(R.drawable.emoji_seestars, "^seestars^");
        emojis.append(R.drawable.emoji_sfyri, "^sfyri^");
        emojis.append(R.drawable.emoji_spam2, "^spam^");
        emojis.append(R.drawable.emoji_esuper, "^super^");
        emojis.append(R.drawable.emoji_tafos, "^tafos^");
        emojis.append(R.drawable.emoji_tomatomourh, "^tomato^");
        emojis.append(R.drawable.emoji_ytold, "^ytold^");
        emojis.append(R.drawable.emoji_beer2, "^beer^");
        emojis.append(R.drawable.emoji_yu, "^yue^");
        emojis.append(R.drawable.emoji_a_eatpaper, "^eatpaper^");
        emojis.append(R.drawable.emoji_fritz, "^fritz^");
        emojis.append(R.drawable.emoji_wade, "^wade^");
        emojis.append(R.drawable.emoji_lypi, "^lypi^");
        emojis.append(R.drawable.emoji_megashok1wq, "^aytoxeir^");
        emojis.append(R.drawable.emoji_victory, "^victory^");
        emojis.append(R.drawable.emoji_filarakia, "^filarakia^");
        emojis.append(R.drawable.emoji_bonjour_97213, "^hat^");
        emojis.append(R.drawable.emoji_curtseyqi9, "^miss^");
        emojis.append(R.drawable.emoji_rofl, "^rolfmao^");
        emojis.append(R.drawable.emoji_question, "^que^");
        emojis.append(R.drawable.emoji_shifty, "^shifty^");
        emojis.append(R.drawable.emoji_shy, "^shy^");
        emojis.append(R.drawable.emoji_music, "^music_listen^");
        emojis.append(R.drawable.emoji_shamed_bag, "^bagface^");
        emojis.append(R.drawable.emoji_rotfl, "^rotate^");
        emojis.append(R.drawable.emoji_love, "^love^");
        emojis.append(R.drawable.emoji_speech, "^speech^");
        emojis.append(R.drawable.emoji_facepalm, "^facepalm^");
        emojis.append(R.drawable.emoji_shocked2, "^shocked^");
        emojis.append(R.drawable.emoji_extremely_shocked, "^ex_shocked^");
        emojis.append(R.drawable.emoji_smurf, "^smurf^");

        GridView emojiGridView = findViewById(R.id.emoji_gridview);
        emojiGridView.setAdapter(new ImageKeyboardAdapter(context, getEmojiArray()));
        emojiGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (inputConnection == null) return;
            String value = emojis.valueAt(position);
            inputConnection.commitText(value, 1);
        });
        AppCompatImageButton backspaceButton = findViewById(R.id.backspace_button);
        // backspace behavior
        final Handler handler = new Handler();
        Runnable longPressed = new Runnable() {
            @Override
            public void run() {
                inputConnection.deleteSurroundingText(1, 0);
                handler.postDelayed(this, 50);
            }
        };
        backspaceButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    CharSequence selectedText = inputConnection.getSelectedText(0);
                    if (TextUtils.isEmpty(selectedText))
                        inputConnection.deleteSurroundingText(1, 0);
                    else
                        inputConnection.commitText("", 1);
                    handler.postDelayed(longPressed, 400);
                    break;
                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(longPressed);
                    break;
            }
            return true;
        });
    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
    }

    public Integer[] getEmojiArray() {
        Integer[] emojiArray = new Integer[emojis.size()];
        for (int i = 0; i < emojiArray.length; i++) {
            emojiArray[i] = emojis.keyAt(i);
        }
        return emojiArray;
    }

    public interface EmojiKeyboardOwner {
        void setEmojiKeyboardVisible(boolean visible);
    }
}
