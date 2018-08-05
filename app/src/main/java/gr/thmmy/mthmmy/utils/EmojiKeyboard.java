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

        emojis.append(R.drawable.smiley, ":)");
        emojis.append(R.drawable.wink, ";)");
        emojis.append(R.drawable.cheesy, ":D");
        emojis.append(R.drawable.grin, ";D");
        // second alias: ^angry^
        emojis.append(R.drawable.angry, ">:(");
        emojis.append(R.drawable.sad, ":(");
        emojis.append(R.drawable.shocked, ":o");
        emojis.append(R.drawable.cool, "8))");
        emojis.append(R.drawable.huh, ":???:");
        emojis.append(R.drawable.rolleyes, "::)");
        emojis.append(R.drawable.tongue, ":P");
        emojis.append(R.drawable.embarrassed, ":-[");
        emojis.append(R.drawable.lipsrsealed, ":-X");
        emojis.append(R.drawable.undecided, ":-\\\\");
        emojis.append(R.drawable.kiss, ":-*");
        emojis.append(R.drawable.cry, ":'(");

        emojis.append(R.drawable.heart, "<3");
        // this was twice in the original page for some reason, with another alias "locked"
        emojis.append(R.drawable.locked, "^lock^");
        emojis.append(R.drawable.roll_over, "^rollover^");
        emojis.append(R.drawable.redface, "^redface^");
        emojis.append(R.drawable.confused, "^confused^");
        emojis.append(R.drawable.innocent, "^innocent^");
        emojis.append(R.drawable.sleep, "^sleep^");
        emojis.append(R.drawable.lips_sealed, "^sealed^");
        emojis.append(R.drawable.cool, "^cool^");
        emojis.append(R.drawable.crazy, "^crazy^");
        emojis.append(R.drawable.mad, "^mad^");
        emojis.append(R.drawable.wav, "^wav^");
        emojis.append(R.drawable.binkybaby, "^binkybaby^");
        emojis.append(R.drawable.police, "^police^");
        emojis.append(R.drawable.dontknow, "^dontknow^");
        // the next two are the same thing?
        emojis.append(R.drawable.angry4, ":angry4:");
        emojis.append(R.drawable.angry_hot, "^angryhot^");
        emojis.append(R.drawable.foyska, "^fouska^");
        emojis.append(R.drawable.e10_7_3e, "^sfinaki^");
        emojis.append(R.drawable.bang_head, "^banghead^");
        emojis.append(R.drawable.crybaby, "^crybaby^");
        emojis.append(R.drawable.hello, "^hello^");
        emojis.append(R.drawable.jerk, "^jerk^");
        emojis.append(R.drawable.nono, "^nono^");
        emojis.append(R.drawable.notworthy, "^notworthy^");
        emojis.append(R.drawable.off_topic, "^off-topic^");
        emojis.append(R.drawable.puke, "^puke^");
        emojis.append(R.drawable.shout, "^shout^");
        emojis.append(R.drawable.slurp, "^slurp^");
        emojis.append(R.drawable.superconfused, "^superconfused^");
        emojis.append(R.drawable.superinnocent, "^superinnocent^");
        emojis.append(R.drawable.cell_phone, "^cellPhone^");
        emojis.append(R.drawable.idiot, "^idiot^");
        emojis.append(R.drawable.knuppel, "^knuppel^");
        emojis.append(R.drawable.tickedoff, "^tickedOff^");
        emojis.append(R.drawable.peace, "^peace^");
        emojis.append(R.drawable.suspicious, "^suspicious^");
        emojis.append(R.drawable.caffine, "^caffine^");
        emojis.append(R.drawable.argue, "^argue^");
        emojis.append(R.drawable.banned2, "^banned2^");
        emojis.append(R.drawable.banned, "^banned^");
        emojis.append(R.drawable.bath, "^bath^");
        emojis.append(R.drawable.beg, "^beg^");
        emojis.append(R.drawable.bluescreen, "^bluescreen^");
        emojis.append(R.drawable.boil, "^boil^");
        emojis.append(R.drawable.bye, "^bye^");
        emojis.append(R.drawable.callmerip, "^callmerip^");
        emojis.append(R.drawable.carnaval, "^carnaval^");
        emojis.append(R.drawable.clap, "^clap^");
        emojis.append(R.drawable.coffeepot, "^coffepot^");
        emojis.append(R.drawable.crap, "^crap^");
        emojis.append(R.drawable.curses, "^curses^");
        emojis.append(R.drawable.funny, "^funny^");
        emojis.append(R.drawable.guitar1, "^guitar^");
        emojis.append(R.drawable.icon_kissy, "^kissy^");
        emojis.append(R.drawable.band, "^band^");
        emojis.append(R.drawable.ivres, "^ivres^");
        emojis.append(R.drawable.kaloe, "^kaloe^");
        emojis.append(R.drawable.kremala, "^kremala^");
        emojis.append(R.drawable.moon, "^moon^");
        emojis.append(R.drawable.mopping, "^mopping^");
        emojis.append(R.drawable.mountza, "^mountza^");
        emojis.append(R.drawable.pcsleep, "^pcsleep^");
        emojis.append(R.drawable.pinokio, "^pinokio^");
        emojis.append(R.drawable.poke, "^poke^");
        emojis.append(R.drawable.seestars, "^seestars^");
        emojis.append(R.drawable.sfyri, "^sfyri^");
        emojis.append(R.drawable.spam2, "^spam^");
        emojis.append(R.drawable.esuper, "^super^");
        emojis.append(R.drawable.tafos, "^tafos^");
        emojis.append(R.drawable.tomatomourh, "^tomato^");
        emojis.append(R.drawable.ytold, "^ytold^");
        emojis.append(R.drawable.beer2, "^beer^");
        emojis.append(R.drawable.yu, "^yue^");
        emojis.append(R.drawable.a_eatpaper, "^eatpaper^");
        emojis.append(R.drawable.fritz, "^fritz^");
        emojis.append(R.drawable.wade, "^wade^");
        emojis.append(R.drawable.lypi, "^lypi^");
        emojis.append(R.drawable.megashok1wq, "^aytoxeir^");
        emojis.append(R.drawable.victory, "^victory^");
        emojis.append(R.drawable.filarakia, "^filarakia^");
        emojis.append(R.drawable.bonjour_97213, "^hat^");
        emojis.append(R.drawable.curtseyqi9, "^miss^");
        emojis.append(R.drawable.rofl, "^rolfmao^");
        emojis.append(R.drawable.question, "^que^");
        emojis.append(R.drawable.shifty, "^shifty^");
        emojis.append(R.drawable.shy, "^shy^");
        emojis.append(R.drawable.music, "^music_listen^");
        emojis.append(R.drawable.shamed_bag, "^bagface^");
        emojis.append(R.drawable.rotfl, "^rotate^");
        emojis.append(R.drawable.love, "^love^");
        emojis.append(R.drawable.speech, "^speech^");
        emojis.append(R.drawable.facepalm, "^facepalm^");
        emojis.append(R.drawable.shocked, "^shocked^");
        emojis.append(R.drawable.extremely_shocked, "^ex_shocked^");
        emojis.append(R.drawable.smurf, "^smurf^");

        GridView emojiGridView = (GridView) findViewById(R.id.emoji_gridview);
        emojiGridView.setAdapter(new ImageKeyboardAdapter(context, getEmojiArray()));
        emojiGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (inputConnection == null) return;
            String value = emojis.valueAt(position);
            inputConnection.commitText(value, 1);
        });
        AppCompatImageButton backspaceButton = (AppCompatImageButton) findViewById(R.id.backspace_button);
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
