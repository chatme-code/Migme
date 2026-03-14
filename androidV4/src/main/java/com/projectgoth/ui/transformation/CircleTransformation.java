/**
 * Copyright (c) 2013 Project Goth
 *
 * CircleTransformation.java
 * Created Mar 24, 2014, 9:03:49 PM
 */

package com.projectgoth.ui.transformation;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;

/**
 * @author mapet
 * 
 */
public class CircleTransformation {

    private static final String KEY = "circleTrans";

    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        float radius = size / 2f;

        final int color = Theme.getColor(ThemeValues.ROUNDED_CORNERS_COLOR);
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawCircle(radius, radius, radius, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    public String key() {
        return KEY;
    }

}
