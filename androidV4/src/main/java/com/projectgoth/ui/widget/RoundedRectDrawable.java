/**
 * Copyright (c) 2013 Project Goth
 *
 * RoundedRectDrawable.java
 * Created Aug 19, 2013, 11:41:05 AM
 */

package com.projectgoth.ui.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Tools;


/**
 * @author dangui
 *
 */
public class RoundedRectDrawable extends ShapeDrawable {
    
    private RoundRectShape rs;
    private Paint fillpaint, strokePaint;
    private RoundedRectParams params;
    
    public RoundedRectDrawable(RoundedRectParams params) {
        super();
        this.params = params;
        init();
    }
    
    public RoundedRectDrawable(int color) {
        super();
        this.params = new RoundedRectParams(color);
        init();
    }

    public RoundedRectDrawable(int color, int cornerRadius) {
        super();
        this.params = new RoundedRectParams(color, cornerRadius);
        init();
    }

    private void init() {
        float[] radii = params.getRadiusArray();
        if (radii == null) {
            int radius = ApplicationEx.getDimension(R.dimen.cornered_box_radius);
            radii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        }
        rs = new RoundRectShape(radii, null, null);
        setShape(rs);
        
        fillpaint = new Paint(this.getPaint());
        fillpaint.setColor(params.getBgColor());
        
        if (params.getBorderWidth() > 0) {
            strokePaint = new Paint(fillpaint);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(Tools.getPixels(params.getBorderWidth()));
            strokePaint.setColor(params.getBorderColor());
        }
    }

    @Override
    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
        shape.draw(canvas, fillpaint);
        if (strokePaint != null) {
            shape.draw(canvas, strokePaint);
        }
    }
    
    public void  setCornerRadiusArray(float[] radiusArray) {
        rs = new RoundRectShape(radiusArray, null, null);
        setShape(rs);
    }
    
    public static class RoundedRectParams {

        private int     bgColor;
        private int     borderWidth;
        private int     borderColor;
        private float[] radiusArray;
        
        public RoundedRectParams() {
        }
        
        public RoundedRectParams(int backgroundColor) {
            this.setBgColor(backgroundColor);
        }

        public RoundedRectParams(int backgroundColor, int radius) {
            this.setBgColor(backgroundColor);
            this.radiusArray = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        }
        /**
         * @return the bgColor
         */
        public int getBgColor() {
            return bgColor;
        }

        /**
         * @param bgColor the bgColor to set
         */
        public void setBgColor(int bgColor) {
            this.bgColor = bgColor;
        }

        /**
         * @return the borderWidth
         */
        public int getBorderWidth() {
            return borderWidth;
        }

        /**
         * @param borderWidth the borderWidth to set
         */
        public void setBorderWidth(int borderWidth) {
            this.borderWidth = borderWidth;
        }

        /**
         * @return the borderColor
         */
        public int getBorderColor() {
            return borderColor;
        }

        /**
         * @param borderColor the borderColor to set
         */
        public void setBorderColor(int borderColor) {
            this.borderColor = borderColor;
        }

        /**
         * @return the radiusArray
         */
        public float[] getRadiusArray() {
            return radiusArray;
        }

        /**
         * @param radiusArray the radiusArray to set
         */
        public void setRadiusArray(float[] radiusArray) {
            this.radiusArray = radiusArray;
        }
    }
    
}
