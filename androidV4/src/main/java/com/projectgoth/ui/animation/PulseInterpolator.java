package com.projectgoth.ui.animation;

import android.view.animation.Interpolator;

public class PulseInterpolator implements Interpolator {
    
    protected Interpolator interpolator;

    @Override
    public float getInterpolation(float input) {
        return getInterpolator().getInterpolation(input < 0.5f? input*2f : 2f*(1f - input));
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }
    
}
