/**
 * Copyright (c) 2013 Project Goth
 *
 * AnimUtils.java
 * Created Jul 2, 2014, 4:06:20 PM
 */

package com.projectgoth.util;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.i18n.I18n;

/**
 * Contains all animation related utilities.
 * 
 * @author angelorohit
 * 
 */
public class AnimUtils {

    private static final String LOG_TAG = AndroidLogger.makeLogTag(AnimUtils.class);
    private static final int DEFAULT_ANIMATION_TIME = 1000;
    
    /**
     * Performs a given {@link Animation} on a given {@link View}.
     * 
     * @param anim
     *            The {@link Animation} to be performed on the given
     *            {@link View}.
     * @param v
     *            The {@link View} on which the animation is to be performed.
     * @param listener
     *            An {@link AnimationListener} for callbacks. Can be null.
     * @return true on success and false otherwise.
     */
    private static boolean doAnimationOnView(Animation anim, View v, 
                                             AnimationListener listener) {
        if (v != null && anim != null) {
            anim.setAnimationListener(listener);
            v.startAnimation(anim);
            return true;
        }

        return false;

    }

    /**
     * Loads an animation from a given animation resource id.
     * 
     * @param id
     *            The resource id of the animation to be loaded.
     * @return The {@link Animation} that was successfully loaded and null on
     *         failure to load the animation.
     */
    private static Animation loadAnimation(int id) {
        try {
            return AnimationUtils.loadAnimation(ApplicationEx.getContext(), id);
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, "Unable to load animation with id:", id);
        }

        return null;
    }

    /**
     * Performs a click scale effect animation on the given {@link View}.
     * 
     * @param v
     *            The {@link View} on which the animation is to be performed.
     * @return true on success and false otherwise.
     */
    public static boolean doClickScaleAnimation(View v, AnimationListener listener) {
        return doAnimationOnView(loadAnimation(R.anim.click_scale_effect), v, listener);
    }

    /**
     * Performs either a fade-in or fade-out animation on the given {@link View}
     * 
     * @param v
     *            The {@link View} on which the animation is to be performed.
     * @param shouldFadeIn
     *            true if the fade-in animation is to be performed and false if
     *            the fade-out animation is to be performed.
     * @return true on success and false otherwise.
     */
    public static boolean doFadeAnimation(View v, boolean shouldFadeIn, AnimationListener listener) {
        if (shouldFadeIn) {
            return doFadeInAnimation(v, listener);
        } else {
            return doFadeOutAnimation(v, listener);
        }
    }

    /**
     * Performs a fade-in animation on the given {@link View}.
     * 
     * @param v
     *            The {@link View} on which the animation is to be performed.
     * @return true on success and false otherwise.
     */
    public static boolean doFadeInAnimation(View v, AnimationListener listener) {
        return doAnimationOnView(loadAnimation(R.anim.fade_in), v, listener);
    }

    /**
     * Performs a fade-out animation on the given {@link View}.
     * 
     * @param v
     *            The {@link View} on which the animation is to be performed.
     * @return true on success and false otherwise.
     */
    public static boolean doFadeOutAnimation(View v, AnimationListener listener) {
        return doAnimationOnView(loadAnimation(R.anim.fade_out), v, listener);
    }

	
	/**
	 * Helper method to calculate the time (in ms) an animation should start from
	 * (i.e. anim.setStartTime(time)) in order to start where its reverse animation
	 * was stopped.
	 * 
	 * @param anim current animation (the one before the reversed animation)
	 * @param drawingTime current drawing time
	 * @param duration duration (in ms) of the new (reverse) animation
	 * @return the time to be set in animation.setStartTime()
	 */
	public static long getReversedStartTime(Animation anim, long drawingTime, long duration) {
		return 2 * (drawingTime - anim.getStartOffset()) - anim.getStartTime() - duration;
	}

    public static void changeTextAnimation(final TextView title, final String originalText, final String changedText) {
        ViewPropertyAnimator.animate(title).alpha(0).setDuration(DEFAULT_ANIMATION_TIME).setListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {
                title.setText(I18n.tr(changedText));
            }
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}
            @Override public void onAnimationEnd(Animator animator) {
                ViewPropertyAnimator.animate(title).alpha(1).setDuration(DEFAULT_ANIMATION_TIME).setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animator) {
                        title.setText(originalText);
                    }
                    @Override public void onAnimationCancel(Animator animator) {}
                    @Override public void onAnimationRepeat(Animator animator) {}
                    @Override public void onAnimationEnd(Animator animator) {} }).start();
            }
        }).start();
    }
    
    public static Animation createSlideToBottomAnimation(final View view, long duration, final AnimationListener listener) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);

        if (duration <= 0) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        animation.setDuration(duration);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setAnimationListener(listener);
        return animation;
    }
    
    public static Animation createSlideFromTopAnimation(final View view, long duration, final AnimationListener listener) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);

        if (duration <= 0) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        animation.setDuration(duration);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setAnimationListener(listener);
        return animation;
    }
    
    public static Animation createSlideFromBottomAnimation(final View view, long duration, final AnimationListener listener) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);

        if (duration <= 0) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        animation.setDuration(duration);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setAnimationListener(listener);

        return animation;
    }
    
    public static Animation createSlideToTopAnimation(final View view, long duration, final AnimationListener listener) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);

        if (duration <= 0) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        animation.setDuration(duration);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setAnimationListener(listener);

        return animation;
    }
}
