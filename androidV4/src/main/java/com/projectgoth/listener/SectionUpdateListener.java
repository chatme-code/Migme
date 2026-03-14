/**
 * Copyright (c) 2013 Project Goth
 *
 * SectionUpdateListener.java
 * Created Oct 17, 2014, 5:19:42 PM
 */

package com.projectgoth.listener;

import com.projectgoth.ui.fragment.GlobalSearchFragment;


/**
 * This is used to indicate that the count of number of results in a section needs to be updated.
 * @see {@link GlobalSearchFragment#setSectionCount(int)}
 * @author angelorohit
 */
public interface SectionUpdateListener {
    public void setSectionCount(final int count);
}
