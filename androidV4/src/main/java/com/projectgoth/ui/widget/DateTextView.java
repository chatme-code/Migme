package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;

/**
 * Created by jtlim on 5/9/14.
 */
public class DateTextView extends TextView {

    private final Paint mPaint;
    private int mTextWidth;
    private int mPaddingBesideText = 30;

    public DateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(ApplicationEx.getDimension(R.dimen.chat_date_line_width));
        mPaint.setColor(ApplicationEx.getColor(R.color.chat_date_line));
        mPaint.setAntiAlias(true);
    }

    public void setText(String text) {
        super.setText(text);
        measure(0,0);

        // Note that this already includes the padding.
        mTextWidth = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getRight() - getLeft();
        if(mTextWidth < width) {
            float midY = 0.5f * (getBottom() - getTop());

            float textLeft = (width + getPaddingLeft() + getPaddingRight() - mPaddingBesideText - mTextWidth) / 2;
            float textRight = (width - getPaddingLeft() - getPaddingRight() + mPaddingBesideText + mTextWidth) / 2;

            canvas.drawLine(getPaddingLeft(), midY, textLeft, midY, mPaint);
            canvas.drawLine(textRight, midY, width-getPaddingRight(), midY, mPaint);
        }
    }

}
