package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Tools;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 *
 * IconCounterView represents a single view showing an icon and a counter
 * The counter will show shortened forms (eg. "12.5k") if useShortCount is on when appropriate
 *
 * XML Tags (xmlns:migme="http://schemas.android.com/apk/res/com.projectgoth")
 *
 * @param migme:iconSrc            - Required -- drawable reference
 * @param migme:iconSize           - Required
 * @param migme:iconToTextSpace    - Defaults to 0
 * @param migme:textSize           - Defaults to dimen.text_size_small
 * @param migme:useShortCount      - Defaults to true
 *
 */
public class IconCounterView extends View implements ImageHandler.ImageLoadListener {

    private final int         mIconSize;
    private final int         mSpacing;
    private final boolean     mUseShortCount;

    private Drawable          mIcon;
    private int               mTextSize;

    private int               mCounter = 0;
    private String            mCounterString = "0";
    private Paint             mTextPaint;
    private float             mTextWidth;
    private float             mTextHeight;
    private float             mTextTop;

    private int               mTextX;
    private int               mTextY;

    private static final int[] EMPTY_STATE = new int[] { };
    private static final int[] SELECTED_STATE = new int[] { android.R.attr.state_selected };

    public IconCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.IconCounterView,
                0, 0);

        try {
            mUseShortCount = a.getBoolean(R.styleable.IconCounterView_useShortCount, true);
            mSpacing = a.getDimensionPixelSize(R.styleable.IconCounterView_iconToTextSpace, 0);
            mTextSize = a.getDimensionPixelSize(R.styleable.IconCounterView_textSize, 0);
            mIconSize = a.getDimensionPixelSize(R.styleable.IconCounterView_iconSize, 0);
            mIcon = a.getDrawable(R.styleable.IconCounterView_iconSrc);
        } finally {
            a.recycle();
        }
        init();
    }

    private final void init() {
        if (mTextSize == 0) {
            mTextSize = ApplicationEx.getDimension(R.dimen.text_size_small);
        }

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(ApplicationEx.getColor(R.color.default_timestamp));
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setIconResource(int drawableResourceId) {
        Resources resources = getResources();
        mIcon = resources.getDrawable(drawableResourceId);
        invalidate();
    }

    public void setIconDrawable(Drawable d) {
        mIcon = d;
        invalidate();
    }

    @SuppressWarnings("deprecation")
    public void setIconBitmap(Bitmap b) {
        mIcon = new BitmapDrawable(b);
        invalidate();
    }

    public void loadIconImage(String url, int defaultDrawableResourceId) {
        setIconResource(defaultDrawableResourceId);
        if(!TextUtils.isEmpty(url)) {
            ImageHandler.getInstance().loadImage(url, this);
        }
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        setIconBitmap(bitmap);
    }
    
    @Override
    public void onImageFailed(ImageView imageView) {
        // Nothing to do.
    }

    public void setCounter(int newValue) {
        if(newValue != mCounter) {
            mCounter = newValue;
            mCounterString = Tools.formatCounters(mCounter, mUseShortCount ? 999 : Integer.MAX_VALUE);
            requestLayout();
        }
    }

    private void measureString() {
        mTextWidth = mTextPaint.measureText(mCounterString);

        // Just to get the height.
        Rect r = new Rect();
        mTextPaint.getTextBounds(mCounterString, 0, mCounterString.length(), r);
        mTextHeight = r.height();
        mTextTop = r.top;
    }

    @Override
    public void setSelected(boolean selected) {

        super.setSelected(selected);
        if (mIcon != null) {
            mIcon.setState(selected ? SELECTED_STATE : EMPTY_STATE);
        }

        final int colorId = selected ? R.color.default_green : R.color.default_timestamp;
        mTextPaint.setColor(ApplicationEx.getColor(colorId));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureString();

        int minw = getPaddingLeft() + getPaddingRight() + mSpacing + mIconSize + (int) Math.ceil(mTextWidth);
        int w = resolveSize(minw, widthMeasureSpec);

        int minh = getPaddingBottom() + getPaddingTop() + (int) Math.max(mIconSize, mTextHeight);
        int h = resolveSize(minh, heightMeasureSpec);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int totalX = (int) (mIconSize + mSpacing + mTextWidth);

        final int iconLeft = (right-left-totalX)/2;
        final int iconTop = (bottom-top-mIconSize)/2;
        
        if (mIcon != null) {
            mIcon.setBounds(iconLeft, iconTop, iconLeft + mIconSize, iconTop + mIconSize);
        }

        mTextX = iconLeft + mIconSize + mSpacing;
        mTextY = (int) ((bottom-top - mTextHeight)/2 - mTextTop);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mIcon != null) {
            mIcon.draw(canvas);
        }
        canvas.drawText(mCounterString, mTextX, mTextY, mTextPaint);
    }
}
