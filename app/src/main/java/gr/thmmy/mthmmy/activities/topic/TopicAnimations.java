package gr.thmmy.mthmmy.activities.topic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

class TopicAnimations {
    //--------------------------POST'S INFO VISIBILITY CHANGE ANIMATION METHOD--------------------------

    /**
     * Method that animates view's visibility changes for post's extra info
     */
    static void animatePostExtraInfoVisibility(final View dateAndPostNum, TextView username,
                                                TextView subject, int expandedColor, int collapsedColor) {
        //If the view is gone fade it in
        if (dateAndPostNum.getVisibility() == View.GONE) {
            //Show full username
            username.setMaxLines(Integer.MAX_VALUE); //As in the android sourcecode
            username.setEllipsize(null);

            //Show full subject
            subject.setTextColor(expandedColor);
            subject.setMaxLines(Integer.MAX_VALUE); //As in the android sourcecode
            subject.setEllipsize(null);


            dateAndPostNum.clearAnimation();
            // Prepare the View for the animation
            dateAndPostNum.setVisibility(View.VISIBLE);
            dateAndPostNum.setAlpha(0.0f);

            // Start the animation
            dateAndPostNum.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            dateAndPostNum.setVisibility(View.VISIBLE);
                        }
                    });
        }
        //If the view is visible fade it out
        else {
            username.setMaxLines(1); //As in the android sourcecode
            username.setEllipsize(TextUtils.TruncateAt.END);

            subject.setTextColor(collapsedColor);
            subject.setMaxLines(1); //As in the android sourcecode
            subject.setEllipsize(TextUtils.TruncateAt.END);

            dateAndPostNum.clearAnimation();

            // Start the animation
            dateAndPostNum.animate()
                    .translationY(dateAndPostNum.getHeight())
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            dateAndPostNum.setVisibility(View.GONE);
                        }
                    });
        }
    }
//------------------------POST'S INFO VISIBILITY CHANGE ANIMATION METHOD END------------------------

//--------------------------USER'S INFO VISIBILITY CHANGE ANIMATION METHOD--------------------------

    /**
     * Method that animates view's visibility changes for user's extra info
     */
    static void animateUserExtraInfoVisibility(final View userExtra) {

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
        }
    }
//------------------------POST'S INFO VISIBILITY CHANGE ANIMATION METHOD END------------------------

}
