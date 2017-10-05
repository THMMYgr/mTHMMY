package gr.thmmy.mthmmy.activities.topic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

class TopicAnimations {
    /**
     * Method that animates view's visibility changes for user's extra info
     */
    static void animateUserExtraInfoVisibility(final TextView username, final TextView subject,
                                               int expandedColor, final int collapsedColor,
                                               final LinearLayout userExtraInfo) {
        //If the view is currently gone it fades it in
        if (userExtraInfo.getVisibility() == View.GONE) {
            userExtraInfo.clearAnimation();
            userExtraInfo.setVisibility(View.VISIBLE);
            userExtraInfo.setAlpha(0.0f);

            //Animation start
            userExtraInfo.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            userExtraInfo.setVisibility(View.VISIBLE);
                        }
                    });

            //Shows full username
            username.setMaxLines(Integer.MAX_VALUE); //As in the android sourcecode
            username.setEllipsize(null);

            //Shows full subject
            subject.setTextColor(expandedColor);
            subject.setMaxLines(Integer.MAX_VALUE); //As in the android sourcecode
            subject.setEllipsize(null);
        }
        //If the view is currently visible then it fades it out
        else {
            userExtraInfo.clearAnimation();

            //Animation start
            userExtraInfo.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            userExtraInfo.setVisibility(View.GONE);

                            //Ellipsizes username
                            username.setMaxLines(1); //As in the android sourcecode
                            username.setEllipsize(TextUtils.TruncateAt.END);

                            //Ellipsizes subject
                            subject.setTextColor(collapsedColor);
                            subject.setMaxLines(1); //As in the android sourcecode
                            subject.setEllipsize(TextUtils.TruncateAt.END);
                        }
                    });
        }
    }
}
