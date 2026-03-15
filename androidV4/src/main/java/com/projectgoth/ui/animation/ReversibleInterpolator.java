package com.projectgoth.ui.animation;

import android.view.animation.Interpolator;

public class ReversibleInterpolator implements Interpolator {
	
	protected Interpolator forward;
	protected final ReversedInterpolator reversed;
	protected Interpolator used = null;
	
	public ReversibleInterpolator(Interpolator other) {
		forward = other;
		reversed = new ReversedInterpolator(other);
		used = forward;
	}
	
	public void setInterpolator(Interpolator other) {
	    if (used == forward) {
	        used = other;
	    }
	    forward = other;
	    reversed.other = other;
	}
	
	public void reverse() {
		reverse(used == forward);
	}
	
	public void reverse(boolean reverse) {
		used = reverse? reversed : forward;
	}
	
	public boolean isReversed() {
		return used == reversed;
	}

	@Override
	public float getInterpolation(float input) {
		return used.getInterpolation(input);
	}
}
