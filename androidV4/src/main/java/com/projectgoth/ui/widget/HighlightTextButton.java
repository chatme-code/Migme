package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import com.projectgoth.R;
import com.projectgoth.i18n.I18n;

/**
 *
 * Button will show localized text, and highlight a portion of it
 *
 * XML Tags (xmlns:migme="http://schemas.android.com/apk/res/com.projectgoth")
 *
 * @param migme:text              - Text that will be translated
 * @param migme:textColor
 * @param migme:highlightText     - Text will be translated
 * @param migme:highlightTextColor
 *
 */
public class HighlightTextButton extends android.widget.Button {

    public HighlightTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        String text;
        String highlightText;
        int color;
        int highlightColor;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HighlightTextButton, 0, 0);

        try {
            text = a.getString(R.styleable.HighlightTextButton_text);
            color = a.getColor(R.styleable.HighlightTextButton_textColor, 0);
            highlightText = a.getString(R.styleable.HighlightTextButton_highlightText);
            highlightColor = a.getColor(R.styleable.HighlightTextButton_highlightTextColor, 0);
        } finally {
            a.recycle();
        }

        // If these crash, then you are missing a migme:text or migme:highlightText attribute. Fix your XML, don't add null checks.
        String textTr = I18n.tr(text);
        String highlightTextTr = I18n.tr(highlightText);

        final SpannableStringBuilder sb = new SpannableStringBuilder(textTr);
        final ForegroundColorSpan span = new ForegroundColorSpan(color);
        final ForegroundColorSpan highlightSpan = new ForegroundColorSpan(highlightColor);

        int indexOfHighlight = textTr.indexOf(highlightTextTr);

        sb.setSpan(span, 0, textTr.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(highlightSpan, indexOfHighlight, indexOfHighlight+highlightTextTr.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        setText(sb);
    }
}
