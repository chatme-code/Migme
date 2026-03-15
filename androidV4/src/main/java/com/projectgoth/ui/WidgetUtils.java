/**
 * Copyright (c) 2013 Project Goth
 *
 * WidgetUtils.java
 * Created 30 Jul, 2014, 11:37:13 am
 */

package com.projectgoth.ui;

import android.content.res.ColorStateList;


/**
 * @author michaeljoos
 * 
 * This class was created intentionally in com.projectgoth.ui so that it is
 * available for all UI widgets. Eclipse will fail to load classes outside this
 * package and thus throw an error when displaying fragments.
 */
public class WidgetUtils {


    /**
     * 
     * @param normal
     * @param pressed
     * @param focused
     * @return
     */
    public static ColorStateList createColorStates(int normal, int pressed, int focused, int disabled) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed },
                new int[] { android.R.attr.state_focused },
                new int[] { -android.R.attr.state_enabled },
                new int[0] };
        int[] colors = new int[] { pressed, focused, disabled, normal };

        return new ColorStateList(states, colors);
    }

}
