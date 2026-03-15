package com.projectgoth.ui.animation;

import android.view.animation.AlphaAnimation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


public class PulseAnimation extends AlphaAnimation {

    public PulseAnimation() {
        super(0f, 1f);
    }

    @Override
    protected void ensureInterpolator() {
        Interpolator interpolator = super.getInterpolator();
        if (interpolator == null || !(interpolator instanceof PulseInterpolator)) {
            interpolator = new PulseInterpolator();
            super.setInterpolator(interpolator);
        }
        PulseInterpolator pulseInterpolator = (PulseInterpolator) interpolator;
        if (pulseInterpolator.interpolator == null) {
            pulseInterpolator.interpolator = new LinearInterpolator();
        }
    }

    @Override
    public void setInterpolator(Interpolator i) {
        PulseInterpolator pulseInterpolator = (PulseInterpolator) super.getInterpolator();
        pulseInterpolator.interpolator = i;
    }

    @Override
    public Interpolator getInterpolator() {
        return ((PulseInterpolator) super.getInterpolator()).interpolator;
    }

}