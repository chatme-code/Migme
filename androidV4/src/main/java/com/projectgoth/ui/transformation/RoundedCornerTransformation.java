/**
 * Copyright (c) 2013 Project Goth
 *
 * RoundedCornerTransformation.java
 * Created 18 Feb, 2014, 6:29:12 pm
 */

package com.projectgoth.ui.transformation;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * @author warrenbalcos
 * 
 */
public class RoundedCornerTransformation {

    private static final String KEY = "roundedCornerTrans";

    public Bitmap transform(Bitmap source) {
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = Theme.getColor(ThemeValues.ROUNDED_CORNERS_COLOR);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, source.getWidth(), source.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = Tools.getPixels(UIUtils.DEFAULT_CORNER_RADIUS);

        paint.setAntiAlias(false);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(source, rect, rect, paint);
        if (source != output) {
            source.recycle();
        }

        return output;
    }

    public String key() {
        return KEY;
    }

}
