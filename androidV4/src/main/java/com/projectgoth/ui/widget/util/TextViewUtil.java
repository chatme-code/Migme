/**
 * Copyright (c) 2013 Project Goth
 *
 * TextViewUtil.java.java
 * Created Jun 19, 2013, 7:46:35 PM
 */

package com.projectgoth.ui.widget.util;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.TextView;
import com.projectgoth.common.Constants;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.Theme;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.widget.ClickableSpanEx;


/**
 * @author cherryv
 * 
 */
public class TextViewUtil {

    /**
     * Create a themeable background for editfields. 
     * 
     * @param applyBorder
     * @param bgDrawable
     * @param border
     * @param bgDrawableHighlight
     * @param borderHighlight
     * @return
     */
    public static StateListDrawable createTextFieldBackgroundTheme(boolean applyBorder, String bgDrawable, String border,
            String bgDrawableHighlight, String borderHighlight) {
        GradientDrawable drawable = (GradientDrawable) Theme.getDrawable(bgDrawable);
        drawable.setStroke(Tools.getPixels(1), Theme.getColor(border));
        drawable.setCornerRadius(Tools.getPixels(UIUtils.DEFAULT_CORNER_RADIUS));

        GradientDrawable drawableHL = (GradientDrawable) Theme.getDrawable(bgDrawableHighlight);
        drawableHL.setStroke(Tools.getPixels(1), Theme.getColor(borderHighlight));
        drawableHL.setCornerRadius(Tools.getPixels(UIUtils.DEFAULT_CORNER_RADIUS));

        StateListDrawable backgroundStates = Tools.createBackgroundStates(drawable, null, drawableHL, null);
        return backgroundStates;
    }

    /**
     * Create a spannable with ellipsize
     *
     * @param text
     * @param txtView
     * @param lines
     * @return
     */
    public static SpannableBuilder.SpannableStringBuilderEx ellipsizeString(SpannableBuilder.SpannableStringBuilderEx text, final TextView txtView, ClickableSpanEx.ClickableSpanExListener listener) {

        // Calculate one word width
        TextPaint paint = txtView.getPaint();
        float width = paint.measureText("a");

        // 300 words width
        int availableTextWidth = (int) (width * 300);

        CharSequence newText = TextUtils.ellipsize(text, paint, availableTextWidth, TextUtils.TruncateAt.END);
        if (text.length() != newText.length()) {
            String more = I18n.tr(Constants.ELLIPSIS_MORE);
            int start = newText.length();
            int end = start + more.length();
            text = new SpannableBuilder.SpannableStringBuilderEx(newText + more);
            ClickableSpanEx clickSpan = new ClickableSpanEx(more, listener);
            text.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.replace(start, end, "\n" + more);
            text.setEllipsize(true);
        }
        return text;
    }

}
