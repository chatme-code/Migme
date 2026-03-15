/**
 * Copyright (c) 2013 Project Goth
 *
 * FragmentEventListener.java.java
 * Created May 30, 2013, 12:31:24 AM
 */
package com.projectgoth.ui.fragment;

import android.support.v4.app.Fragment;


/**
 * @author cherryv
 *
 */
public interface FragmentEventListener {
	
	public void onShowFragment (Fragment fragment);
	
	public void onHideFragment (Fragment fragment);
	
	public void onDetachFragment (String fragmentTag);

}
