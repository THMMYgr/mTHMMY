package gr.thmmy.mthmmy.activities.topic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

class TopicAnimations {
//--------------------------USER'S INFO VISIBILITY CHANGE ANIMATION METHOD--------------------------

    /**
     * Method that animates view's visibility changes for user's extra info
     */
    static void animateUserExtraInfoVisibility(TextView username, TextView subject,
                                               int expandedColor, int collapsedColor,
                                               final View userExtra) {
        //If the view is gone fade it in
        if (userExtra.getVisibility() == View.GONE) {
            userExtra.clearAnimation();
            userExtra.setVisibility(View.VISIBLE);
            userExtra.setAlpha(0.0f);

            // Start the animation
            userExtra.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            userExtra.setVisibility(View.VISIBLE);
                        }
                    });

            //Show full username
            username.setMaxLines(Integer.MAX_VALUE); //As in the android sourcecode
            username.setEllipsize(null);

            //Show full subject
            subject.setTextColor(expandedColor);
            subject.setMaxLines(Integer.MAX_VALUE); //As in the android sourcecode
            subject.setEllipsize(null);
        }
        //If the view is visible fade it out
        else {
            userExtra.clearAnimation();

            // Start the animation
            userExtra.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            userExtra.setVisibility(View.GONE);
                        }
                    });

            username.setMaxLines(1); //As in the android sourcecode
            username.setEllipsize(TextUtils.TruncateAt.END);

            subject.setTextColor(collapsedColor);
            subject.setMaxLines(1); //As in the android sourcecode
            subject.setEllipsize(TextUtils.TruncateAt.END);
        }
    }
//------------------------POST'S INFO VISIBILITY CHANGE ANIMATION METHOD END------------------------

}
