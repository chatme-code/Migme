/**
 * Copyright (c) 2013 Project Goth
 *
 * ButtonUtil.java.java
 * Created Jun 19, 2013, 7:44:25 PM
 */

package com.projectgoth.ui.widget.util;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.ui.WidgetUtils;

/**
 * @author cherryv
 * 
 */
public class ButtonUtil {

    public static final String BUTTON_TYPE_ORANGE               = "button_orange";
    public static final String BUTTON_TYPE_TURQUOISE            = "button_turquoise";
    public static final String BUTTON_TYPE_GRAY                 = "button_gray";

    /**
     * Creates a themeable drawable background for buttons. Assumption is a
     * button has a gradient background and rounded borders.
     * 
     * @param bgDrawable
     * @param border
     * @param bgDrawableHighlight
     * @param borderHighlight
     * @param cornerRadius
     * @return
     */
    public static StateListDrawable createButtonBackground(String bgDrawable, String border,
            String bgDrawableHighlight, String borderHighlight, int cornerRadius) {
        final int cornerRadiusPx = Tools.getPixels(cornerRadius);
        GradientDrawable drawable = (GradientDrawable) Theme.getDrawable(bgDrawable);
        if (drawable != null) {
            drawable.setStroke(Tools.getPixels(1), Theme.getColor(border));
            drawable.setCornerRadius(cornerRadiusPx);
        }


        GradientDrawable drawableHL = (GradientDrawable) Theme.getDrawable(bgDrawableHighlight);
        if (drawableHL != null) {
            drawableHL.setStroke(Tools.getPixels(1), Theme.getColor(borderHighlight));
            drawableHL.setCornerRadius(cornerRadiusPx);
        }

        GradientDrawable disabled = (GradientDrawable) Theme.getDrawable(ThemeValues.DISABLED_BUTTON_BG);
        if (disabled != null) {
            disabled.setStroke(Tools.getPixels(1), Theme.getColor(ThemeValues.DISABLED_BUTTON_BORDER));
            disabled.setCornerRadius(cornerRadius);
        }

        return Tools.createBackgroundStates(drawable, drawableHL, drawableHL, disabled);
    }

    /**
     * Returns pre-defined button themes. Default theme is
     * {@link #BUTTON_TYPE_NORMAL}
     * 
     * @param buttonTheme
     *            One of the known {@link ButtonUtil} themes.
     * @param cornerRadius
     *            The corner radius to be used when creating the buttons
     *            background theme.
     * @return A {@link StateListDrawable} which represents the themed button
     *         background.
     */
    public static StateListDrawable getButtonBGTheme(String buttonTheme, int cornerRadius) {
        if (buttonTheme != null) {
            if (buttonTheme.equals(BUTTON_TYPE_ORANGE)) {
                return createButtonBackground(ThemeValues.ORANGE_BUTTON_BG_NORMAL,
                        ThemeValues.ORANGE_BUTTON_BORDER_NORMAL, ThemeValues.ORANGE_BUTTON_BG_HIGHLIGHT,
                        ThemeValues.ORANGE_BUTTON_BORDER_HIGHLIGHT, cornerRadius);
            } else if (buttonTheme.equals(BUTTON_TYPE_TURQUOISE)) {
                return createButtonBackground(ThemeValues.TURQUOISE_BUTTON_BG_NORMAL,
                        ThemeValues.TURQUOISE_BUTTON_BORDER_NORMAL, ThemeValues.TURQUOISE_BUTTON_BG_HIGHLIGHT,
                        ThemeValues.TURQUOISE_BUTTON_BORDER_HIGHLIGHT, cornerRadius);
            } else if (buttonTheme.equals(BUTTON_TYPE_GRAY)) {
                return createButtonBackground(ThemeValues.GRAY_BUTTON_BACKGROUND_NORMAL,
                        ThemeValues.GRAY_BUTTON_BORDER_NORMAL, ThemeValues.GRAY_BUTTON_BACKGROUND_HIGHLIGHT,
                        ThemeValues.GRAY_BUTTON_BORDER_HIGHLIGHT, cornerRadius);
            }
        }

        return createButtonBackground(ThemeValues.ORANGE_BUTTON_BG_NORMAL, ThemeValues.ORANGE_BUTTON_BORDER_NORMAL,
                ThemeValues.ORANGE_BUTTON_BG_HIGHLIGHT, ThemeValues.ORANGE_BUTTON_BORDER_HIGHLIGHT, cornerRadius);
    }

    public static ColorStateList getButtonTextTheme(String buttonTheme) {
        if (buttonTheme != null) {
            if (buttonTheme.equals(BUTTON_TYPE_ORANGE)) {
                return WidgetUtils.createColorStates(Theme.getColor(ThemeValues.ORANGE_BUTTON_TEXT_NORMAL),
                        Theme.getColor(ThemeValues.ORANGE_BUTTON_TEXT_HIGHLIGHT),
                        Theme.getColor(ThemeValues.ORANGE_BUTTON_TEXT_HIGHLIGHT),
                        Theme.getColor(ThemeValues.DISABLED_BUTTON_TEXT));
            } else if (buttonTheme.equals(BUTTON_TYPE_TURQUOISE)) {
                return WidgetUtils.createColorStates(Theme.getColor(ThemeValues.TURQUOISE_BUTTON_TEXT_NORMAL),
                        Theme.getColor(ThemeValues.TURQUOISE_BUTTON_TEXT_HIGHLIGHT),
                        Theme.getColor(ThemeValues.TURQUOISE_BUTTON_TEXT_HIGHLIGHT),
                        Theme.getColor(ThemeValues.TURQUOISE_BUTTON_TEXT_HIGHLIGHT));
            } else if (buttonTheme.equals(BUTTON_TYPE_GRAY)) {
                return WidgetUtils.createColorStates(Theme.getColor(ThemeValues.GRAY_BUTTON_TEXT_NORMAL),
                        Theme.getColor(ThemeValues.GRAY_BUTTON_TEXT_HIGHLIGHT),
                        Theme.getColor(ThemeValues.GRAY_BUTTON_TEXT_HIGHLIGHT),
                        Theme.getColor(ThemeValues.DISABLED_BUTTON_TEXT));
            }
        }

        return WidgetUtils.createColorStates(Theme.getColor(ThemeValues.ORANGE_BUTTON_TEXT_NORMAL),
                Theme.getColor(ThemeValues.ORANGE_BUTTON_TEXT_HIGHLIGHT),
                Theme.getColor(ThemeValues.ORANGE_BUTTON_TEXT_HIGHLIGHT),
                Theme.getColor(ThemeValues.DISABLED_BUTTON_TEXT));
    }

    public static Drawable getButtonIcon(String buttonTheme) {
        return null;
    }

    public static Drawable getButtonIcon(int iconResId) {
        Drawable iconDrawable = ApplicationEx.getContext().getResources().getDrawable(iconResId);
        iconDrawable.setBounds(0, 0, ApplicationEx.getDimension(R.dimen.button_icon_width),
                ApplicationEx.getDimension(R.dimen.button_icon_height));
        return iconDrawable;
    }

}
