package gr.thmmy.mthmmy.views.editorview;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;

import gr.thmmy.mthmmy.R;

public class EmojiKeyboard extends LinearLayout implements IEmojiKeyboard {

    // TODO: Sort emojis in a way that makes sense
    private final Emoji[] emojis = {new Emoji(R.drawable.emoji_smiley, ":)"),
            new Emoji(R.drawable.emoji_wink, ";)"),
            new Emoji(R.drawable.emoji_cheesy, ":D"),
            new Emoji(R.drawable.emoji_grin, ";D"),
            // removed repeated angry emoji
            new Emoji(R.drawable.emoji_angry, ">:("),
            new Emoji(R.drawable.emoji_sad, ":("),
            new Emoji(R.drawable.emoji_shocked, ":o"),
            new Emoji(R.drawable.emoji_cool, "8))"),
            new Emoji(R.drawable.emoji_huh, ":???:"),
            new Emoji(R.drawable.emoji_rolleyes, "::)"),
            new Emoji(R.drawable.emoji_tongue, ":P"),
            new Emoji(R.drawable.emoji_embarrassed, ":-["),
            new Emoji(R.drawable.emoji_lipsrsealed, ":-X"),
            new Emoji(R.drawable.emoji_undecided, ":-\\\\"),
            new Emoji(R.drawable.emoji_kiss, ":-*"),
            new Emoji(R.drawable.emoji_cry, ":'("),
            new Emoji(R.drawable.emoji_heart, "<3"),
            // removed repeated lock emoji
            new Emoji(R.drawable.emoji_locked, "^lock^"),
            new Emoji(R.drawable.emoji_roll_over, "^rollover^"),
            new Emoji(R.drawable.emoji_redface, "^redface^"),
            new Emoji(R.drawable.emoji_confused, "^confused^"),
            new Emoji(R.drawable.emoji_innocent, "^innocent^"),
            new Emoji(R.drawable.emoji_sleep, "^sleep^"),
            new Emoji(R.drawable.emoji_lips_sealed, "^sealed^"),
            new Emoji(R.drawable.emoji_cool2, "^cool^"),
            new Emoji(R.drawable.emoji_monster, "^monster^"),
            new Emoji(R.drawable.emoji_crazy, "^crazy^"),
            new Emoji(R.drawable.emoji_mad, "^mad^"),
            new Emoji(R.drawable.emoji_wav, "^wav^"),
            new Emoji(R.drawable.emoji_binkybaby, "^binkybaby^"),
            new Emoji(R.drawable.emoji_police, "^police^"),
            new Emoji(R.drawable.emoji_dontknow, "^dontknow^"),
            //removed repeated angry hot emoji
            new Emoji(R.drawable.emoji_angry_hot, "^angryhot^"),
            new Emoji(R.drawable.emoji_foyska, "^fouska^"),
            new Emoji(R.drawable.emoji_e10_7_3e, "^sfinaki^"),
            new Emoji(R.drawable.emoji_bang_head, "^banghead^"),
            new Emoji(R.drawable.emoji_crybaby, "^crybaby^"),
            new Emoji(R.drawable.emoji_hello, "^hello^"),
            new Emoji(R.drawable.emoji_jerk, "^jerk^"),
            new Emoji(R.drawable.emoji_nono, "^nono^"),
            new Emoji(R.drawable.emoji_notworthy, "^notworthy^"),
            new Emoji(R.drawable.emoji_off_topic, "^off-topic^"),
            new Emoji(R.drawable.emoji_puke, "^puke^"),
            new Emoji(R.drawable.emoji_shout, "^shout^"),
            new Emoji(R.drawable.emoji_slurp, "^slurp^"),
            new Emoji(R.drawable.emoji_superconfused, "^superconfused^"),
            new Emoji(R.drawable.emoji_superinnocent, "^superinnocent^"),
            new Emoji(R.drawable.emoji_cell_phone, "^cellPhone^"),
            new Emoji(R.drawable.emoji_idiot, "^idiot^"),
            new Emoji(R.drawable.emoji_knuppel, "^knuppel^"),
            new Emoji(R.drawable.emoji_tickedoff, "^tickedOff^"),
            new Emoji(R.drawable.emoji_peace, "^peace^"),
            new Emoji(R.drawable.emoji_suspicious, "^suspicious^"),
            new Emoji(R.drawable.emoji_caffine, "^caffine^"),
            new Emoji(R.drawable.emoji_argue, "^argue^"),
            new Emoji(R.drawable.emoji_banned2, "^banned2^"),
            new Emoji(R.drawable.emoji_banned, "^banned^"),
            new Emoji(R.drawable.emoji_bath, "^bath^"),
            new Emoji(R.drawable.emoji_beg, "^beg^"),
            new Emoji(R.drawable.emoji_bluescreen, "^bluescreen^"),
            new Emoji(R.drawable.emoji_boil, "^boil^"),
            new Emoji(R.drawable.emoji_bye, "^bye^"),
            new Emoji(R.drawable.emoji_callmerip, "^callmerip^"),
            new Emoji(R.drawable.emoji_carnaval, "^carnaval^"),
            new Emoji(R.drawable.emoji_clap, "^clap^"),
            new Emoji(R.drawable.emoji_coffeepot, "^coffepot^"),
            new Emoji(R.drawable.emoji_crap, "^crap^"),
            new Emoji(R.drawable.emoji_curses, "^curses^"),
            new Emoji(R.drawable.emoji_funny, "^funny^"),
            new Emoji(R.drawable.emoji_guitar1, "^guitar^"),
            new Emoji(R.drawable.emoji_icon_kissy, "^kissy^"),
            new Emoji(R.drawable.emoji_band, "^band^"),
            new Emoji(R.drawable.emoji_ivres, "^ivres^"),
            new Emoji(R.drawable.emoji_kaloe, "^kaloe^"),
            new Emoji(R.drawable.emoji_kremala, "^kremala^"),
            new Emoji(R.drawable.emoji_moon, "^moon^"),
            new Emoji(R.drawable.emoji_mopping, "^mopping^"),
            new Emoji(R.drawable.emoji_mountza, "^mountza^"),
            new Emoji(R.drawable.emoji_pcsleep, "^pcsleep^"),
            new Emoji(R.drawable.emoji_pinokio, "^pinokio^"),
            new Emoji(R.drawable.emoji_poke, "^poke^"),
            new Emoji(R.drawable.emoji_seestars, "^seestars^"),
            new Emoji(R.drawable.emoji_sfyri, "^sfyri^"),
            new Emoji(R.drawable.emoji_spam2, "^spam^"),
            new Emoji(R.drawable.emoji_esuper, "^super^"),
            new Emoji(R.drawable.emoji_tafos, "^tafos^"),
            new Emoji(R.drawable.emoji_tomatomourh, "^tomato^"),
            new Emoji(R.drawable.emoji_ytold, "^ytold^"),
            new Emoji(R.drawable.emoji_beer2, "^beer^"),
            new Emoji(R.drawable.emoji_yu, "^yue^"),
            new Emoji(R.drawable.emoji_a_eatpaper, "^eatpaper^"),
            new Emoji(R.drawable.emoji_fritz, "^fritz^"),
            new Emoji(R.drawable.emoji_wade, "^wade^"),
            new Emoji(R.drawable.emoji_lypi, "^lypi^"),
            new Emoji(R.drawable.emoji_megashok1wq, "^aytoxeir^"),
            new Emoji(R.drawable.emoji_victory, "^victory^"),
            new Emoji(R.drawable.emoji_filarakia, "^filarakia^"),
            new Emoji(R.drawable.emoji_bonjour_97213, "^hat^"),
            new Emoji(R.drawable.emoji_curtseyqi9, "^miss^"),
            new Emoji(R.drawable.emoji_rofl, "^rolfmao^"),
            new Emoji(R.drawable.emoji_question, "^que^"),
            new Emoji(R.drawable.emoji_shifty, "^shifty^"),
            new Emoji(R.drawable.emoji_shy, "^shy^"),
            new Emoji(R.drawable.emoji_music, "^music_listen^"),
            new Emoji(R.drawable.emoji_shamed_bag, "^bagface^"),
            new Emoji(R.drawable.emoji_rotfl, "^rotate^"),
            new Emoji(R.drawable.emoji_love, "^love^"),
            new Emoji(R.drawable.emoji_speech, "^speech^"),
            new Emoji(R.drawable.emoji_facepalm, "^facepalm^"),
            new Emoji(R.drawable.emoji_shocked2, "^shocked^"),
            new Emoji(R.drawable.emoji_extremely_shocked, "^ex_shocked^"),
            new Emoji(R.drawable.emoji_smurf, "^smurf^")
    };

    private InputConnection inputConnection;
    private HashSet<EmojiInputField> emojiInputFields = new HashSet<>();
    private Context context;

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
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.emoji_keyboard, this, true);
        setOrientation(VERTICAL);
        setBackgroundColor(getResources().getColor(R.color.primary));

        RecyclerView emojiRecyclerview = findViewById(R.id.emoji_recyclerview);
        emojiRecyclerview.setHasFixedSize(true);
        GridLayoutManager emojiLayoutManager = new GridLayoutManager(context, 6);
        emojiLayoutManager.setSpanSizeLookup(new EmojiColumnSpanLookup());
        emojiRecyclerview.setLayoutManager(emojiLayoutManager);

        EmojiKeyboardAdapter emojiKeyboardAdapter = new EmojiKeyboardAdapter(emojis);
        emojiKeyboardAdapter.setOnEmojiClickListener((view, position) -> {
            if (inputConnection == null) return;
            String bbcode = emojis[position].getBbcode();
            inputConnection.commitText(" " + bbcode, 1);
        });
        emojiRecyclerview.setAdapter(emojiKeyboardAdapter);
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

    @Override
    public void hide() {
        setVisibility(GONE);
    }

    @Override
    public void registerEmojiInputField(EmojiInputField emojiInputField) {
        emojiInputFields.add(emojiInputField);
    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
    }

    @Override
    public boolean onEmojiButtonToggle() {
        if (getVisibility() == VISIBLE) setVisibility(GONE);
        else setVisibility(VISIBLE);
        return getVisibility() == VISIBLE;
    }

    @Override
    public void onEmojiInputFieldFocused(EmojiInputField emojiInputField) {
        setInputConnection(emojiInputField.getInputConnection());
    }

    @Override
    public void setVisibility(int visibility) {
        //notify input fields
        for (EmojiInputField emojiInputField : emojiInputFields)
            emojiInputField.onKeyboardVisibilityChange(visibility == VISIBLE);
        super.setVisibility(visibility);
    }

    @Override
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    class Emoji {
        final int src;
        final String bbcode;

        public Emoji(int src, String bbcode) {
            this.src = src;
            this.bbcode = bbcode;
        }

        public int getSrc() {
            return src;
        }

        public String getBbcode() {
            return bbcode;
        }
    }

    class EmojiColumnSpanLookup extends GridLayoutManager.SpanSizeLookup {

        @Override
        public int getSpanSize(int position) {
            switch (emojis[position].getSrc()) {
                case R.drawable.emoji_wav:
                    return 4;
                case R.drawable.emoji_band:
                    return 3;
                case R.drawable.emoji_pcsleep:
                    return 2;
                default:
                    return 1;
            }
        }
    }
}
