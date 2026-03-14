/**
 * Copyright (c) 2013 Project Goth
 *
 * OffsetLayoutParams.java
 * Created 18 Sep, 2014, 4:51:35 pm
 */

package com.projectgoth.ui.widget.allaccessbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import com.projectgoth.R;


/**
 * @author michaeljoos
 *
 */
public class OffsetLayoutParams extends LayoutParams {
    /**
     * The horizontal, or X, offset of the child within the view group.
     */
    public int x;
    
    /**
     * The vertical, or Y, offset of the child within the view group.
     */
    public int y;
    
    public OffsetLayoutParams(LayoutParams source) {
        super(source);
    }
    
    public OffsetLayoutParams(Context c, AttributeSet attrs) {
        super(c, attrs);
        
        TypedArray a =
                c.obtainStyledAttributes(attrs, R.styleable.OffsetLayout);

        // Use getDimension instead of getDimensionPixelSize to avoid negative
        // values getting a 1 pixel offset.
        x = a.getDimensionPixelOffset(R.styleable.OffsetLayout_offsetX, 0);
        y = a.getDimensionPixelOffset(R.styleable.OffsetLayout_offsetY, 0);

        a.recycle();
    }

    public OffsetLayoutParams(int width, int height) {
        super(width, height);
    }

    /**
     * Creates a new set of layout parameters with the specified width,
     * height and offset.
     *
     * @param width the width, either {@link #MATCH_PARENT},
              {@link #WRAP_CONTENT} or a fixed size in pixels
     * @param height the height, either {@link #MATCH_PARENT},
              {@link #WRAP_CONTENT} or a fixed size in pixels
     * @param x the X offset of the child
     * @param y the Y offset of the child
     */
    public OffsetLayoutParams(int width, int height, int x, int y) {
        super(width, height);
        this.x = x;
        this.y = y;
    }

    public static int getChildMeasureSpec(int childSize, int maxAvailable) {
        int childSpecMode = 0;
        int childSpecSize = 0;

        if (childSize >= 0) {
            // Child wanted an exact size. Give as much as possible
            childSpecMode = MeasureSpec.EXACTLY;
            if (maxAvailable >= 0) {
                childSpecSize = Math.min(maxAvailable, childSize);
            } else {
                childSpecSize = childSize;
            }
        } else if (childSize == LayoutParams.MATCH_PARENT) {
            childSpecMode = MeasureSpec.EXACTLY;
            childSpecSize = maxAvailable;
        } else if (childSize == LayoutParams.WRAP_CONTENT) {
            if (maxAvailable >= 0) {
                // We have a maximum size in this dimension.
                childSpecMode = MeasureSpec.AT_MOST;
                childSpecSize = maxAvailable;
            } else {
                // Child can be as big as it wants
                childSpecMode = MeasureSpec.UNSPECIFIED;
                childSpecSize = 0;
            }
        }

        return MeasureSpec.makeMeasureSpec(childSpecSize, childSpecMode);
    }
}
