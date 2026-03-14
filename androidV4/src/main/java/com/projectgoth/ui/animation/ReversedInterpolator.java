package com.projectgoth.ui.animation;

import android.view.animation.Interpolator;

public class ReversedInterpolator implements Interpolator {
	
	public Interpolator other;
	
	public ReversedInterpolator(Interpolator other) {
		this.other = other;
	}

	@Override
	public float getInterpolation(float input) {
		return other.getInterpolation(1f - input);
	}
}