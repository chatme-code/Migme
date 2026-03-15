package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;

/**
 *
 * Button will show localized text
 *
 * XML Tags (xmlns:migme="http://schemas.android.com/apk/res/com.projectgoth")
 *
 * @param migme:text              - Text that will be translated
 * @param migme:backgroundColor   - Background color
 * @param migme:cornerRadius      - Background color
 *
 */
public class TextButton extends android.widget.Button {

    public TextButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        String text;
        int backgroundColor;
        int cornerRadius;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TextButton, 0, 0);

        try {
            text = a.getString(R.styleable.TextButton_text);
            backgroundColor = a.getColor(R.styleable.TextButton_backgroundColor, 0);
            cornerRadius = a.getDimensionPixelSize(R.styleable.TextButton_cornerRadius, 0);
        } finally {
            a.recycle();
        }

        // If this crashes, then you are missing a migme:text attribute. Fix your XML, don't add null checks.
        setText(I18n.tr(text));

        if(cornerRadius == 0) {
            setBackgroundColor(backgroundColor);
        } else {
            UIUtils.setBackground(this, new RoundedRectDrawable(backgroundColor, cornerRadius));
        }
    }
}
